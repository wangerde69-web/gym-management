# YOLOv8-pose 人体姿态分析脚本：基于 ONNX 模型进行关键点检测，计算关节角度并提供运动康复建议
import cv2
import numpy as np
import json
import sys
import math
import os
import argparse
import urllib.request
import urllib.error

# YOLOv8-pose COCO 关键点索引
# COCO 数据集 17 个关键点索引映射
KP = {
    "NOSE": 0, "LEFT_EYE": 1, "RIGHT_EYE": 2,
    "LEFT_EAR": 3, "RIGHT_EAR": 4,
    "LEFT_SHOULDER": 5, "RIGHT_SHOULDER": 6,
    "LEFT_ELBOW": 7, "RIGHT_ELBOW": 8,
    "LEFT_WRIST": 9, "RIGHT_WRIST": 10,
    "LEFT_HIP": 11, "RIGHT_HIP": 12,
    "LEFT_KNEE": 13, "RIGHT_KNEE": 14,
    "LEFT_ANKLE": 15, "RIGHT_ANKLE": 16,
}

# 骨骼连线定义：用于绘制骨架结构的关节对索引
BONES = [
    (5, 6), (5, 11), (6, 12), (11, 12),
    (5, 7), (7, 9), (6, 8), (8, 10),
    (11, 13), (13, 15), (12, 14), (14, 16),
    (0, 1), (0, 2), (1, 3), (2, 4),
]

# 模型文件路径与脚本目录
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
ONNX_PATH = os.path.join(SCRIPT_DIR, "..", "yolov8n-pose.onnx")

# ---------- LLM 大模型增强分析：通过外部 API 对规则结果进行专业化改写 ----------

SYSTEM_PROMPT = """你是资深运动康复与体能训练专家。用户提供一份体态分析数据（JSON），包含各关节角度和初步检测结果。

你需要完成两件事：
1. 根据角度数据自动识别用户正在做什么动作（深蹲、硬拉、弓步蹲、卧推、推举、划船等），在 issues 第一条加上动作标签，如"[深蹲] 综合评分 85/100 — 身体姿态对称性良好"
2. 基于该动作的生物力学标准，完全重写 issues 和 suggestions，使其专业、准确、有针对性

规则：
- 动作识别参考：膝角<120°且躯干前倾→深蹲；膝角>130°、髋角<60°且躯干大幅前倾→硬拉；左右膝角差异大且一侧膝角<100°→弓步蹲；躯干接近直立→可能推举/实力推；综合分析即可
- 不要被原始 issues/suggestions 中的错误姿势名称误导，你必须根据角度数据自行判断动作
- 角度数据和百分比数值必须保留
- 使用第二人称"你"，语气专业但亲切
- 返回纯 JSON，格式与输入完全一致
- 不要添加额外解释"""


# 调用 LLM API 对初步分析结果进行运动康复专业化改写
def call_llm(api_url, model, data):
    url = api_url.rstrip("/") + "/chat/completions"
    body = {
        "model": model,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": json.dumps(data, ensure_ascii=False)}
        ],
        "temperature": 0.3,
        "max_tokens": 2048
    }
    req = urllib.request.Request(
        url,
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json"}
    )
    try:
        with urllib.request.urlopen(req, timeout=45) as resp:
            raw = json.loads(resp.read().decode("utf-8"))
            content = raw["choices"][0]["message"]["content"].strip()
            if content.startswith("```"):
                lines = content.split("\n")
                content = "\n".join(lines[1:-1] if lines[-1].strip() == "```" else lines[1:])
            return json.loads(content)
    except Exception:
        return None


# ---------- 几何计算工具：计算三点夹角 ----------

# 计算三点夹角（以 b 为顶点，计算 ba 与 bc 的夹角，返回角度制）
def angle_between(a, b, c):
    ba = np.array(a, dtype=np.float64) - np.array(b, dtype=np.float64)
    bc = np.array(c, dtype=np.float64) - np.array(b, dtype=np.float64)
    cos_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc) + 1e-8)
    cos_angle = max(-1.0, min(1.0, float(cos_angle)))
    return math.degrees(math.acos(cos_angle))


# ---------- YOLOv8-pose 模型推理：图片预处理与推理结果后处理 ----------

# 图片预处理：等比缩放至 640x640 并居中填充灰色背景，输出归一化 NCHW 格式
def preprocess(image):
    h, w = image.shape[:2]
    scale = min(640 / h, 640 / w)
    new_h, new_w = int(h * scale), int(w * scale)
    resized = cv2.resize(image, (new_w, new_h))

    canvas = np.full((640, 640, 3), 114, dtype=np.uint8)
    dy = (640 - new_h) // 2
    dx = (640 - new_w) // 2
    canvas[dy:dy + new_h, dx:dx + new_w] = resized

    blob = canvas.astype(np.float32) / 255.0
    blob = blob.transpose(2, 0, 1)
    blob = np.expand_dims(blob, 0)
    return blob, scale, dx, dy


# 推理结果后处理：解析模型输出，提取 17 个关键点的坐标与置信度，还原到原始图片坐标系
def postprocess(output, scale, dx, dy, img_w, img_h, conf_thres=0.3):
    import onnxruntime as ort
    preds = output[0]
    preds = np.transpose(preds[0], (1, 0))

    kpts_all = []
    confs_all = []

    for pred in preds:
        box_conf = pred[4]
        if box_conf < conf_thres:
            kpts_all.append(np.zeros((17, 2), dtype=np.float32))
            confs_all.append(np.zeros(17, dtype=np.float32))
            continue

        kpts = np.zeros((17, 2), dtype=np.float32)
        confs = np.zeros(17, dtype=np.float32)
        for i in range(17):
            kx = (pred[5 + i * 3] - dx) / scale
            ky = (pred[6 + i * 3] - dy) / scale
            kc = pred[7 + i * 3]
            kpts[i] = [kx, ky]
            confs[i] = kc
        kpts_all.append(kpts)
        confs_all.append(confs)

    return np.array(kpts_all), np.array(confs_all)


# ---------- 基于规则的体态分析：根据关节角度计算综合评分并生成问题与建议 ----------

# 规则兜底分析：根据关键点坐标和关节角度，生成体态评估问题列表与改善建议
def generate_rule_based(kpts, angles):
    issues = []
    suggestions = []

    # 计算左右对称性指标与偏差程度
    avg_knee = float((angles["left_knee"] + angles["right_knee"]) / 2)
    knee_diff = float(abs(angles["left_knee"] - angles["right_knee"]))
    avg_trunk = float((angles["trunk_lean_left"] + angles["trunk_lean_right"]) / 2)
    avg_hip = float((angles["left_hip"] + angles["right_hip"]) / 2)
    hip_diff = float(abs(angles["left_hip"] - angles["right_hip"]))
    left_hip_y = float(kpts[KP["LEFT_HIP"]][1])
    right_hip_y = float(kpts[KP["RIGHT_HIP"]][1])
    hip_tilt = float(abs(left_hip_y - right_hip_y))

    # 严重程度评估辅助函数：根据偏差量计算风险百分比
    def severity(deviation, threshold, cap):
        ratio = min(deviation / threshold, cap) / cap
        return round(ratio * 100, 1)

    # 综合评分
    total_score = 100.0
    total_score -= knee_diff * 0.6
    total_score -= hip_tilt * 0.3
    total_score -= hip_diff * 0.3
    if avg_trunk < 10 or avg_trunk > 70:
        total_score -= 15
    total_score = max(0, min(100, round(total_score, 1)))

    if total_score >= 90:
        issues.append(f"综合评分 {total_score}/100 — 身体姿态对称性良好")
    elif total_score >= 70:
        issues.append(f"综合评分 {total_score}/100 — 存在轻微不平衡")
    elif total_score >= 50:
        issues.append(f"综合评分 {total_score}/100 — 需要针对性调整")
    else:
        issues.append(f"综合评分 {total_score}/100 — 建议在教练指导下调整")

    # 对称性
    # 左右膝关节对称性检测与重心偏移评估
    if knee_diff > 15:
        lean = "左侧" if angles["left_knee"] > angles["right_knee"] else "右侧"
        issues.append(f"左右膝关节角度严重不对称（左{angles['left_knee']}° / 右{angles['right_knee']}°，差{knee_diff:.0f}°），重心偏向{lean}")
        suggestions.append(f"重心偏向{lean}，建议进行单侧稳定性训练矫正左右不平衡")
    elif knee_diff > 8:
        lean = "左侧" if angles["left_knee"] > angles["right_knee"] else "右侧"
        issues.append(f"左右膝关节角度轻微不对称（差{knee_diff:.0f}°），{lean}受力较多")
        suggestions.append(f"{lean}受力较多，训练时注意双脚压力均匀分布")

    # 左右髋关节对称性与骨盆旋转检测
    if hip_diff > 12:
        issues.append(f"左右髋关节角度不对称（差{hip_diff:.0f}°），可能存在骨盆旋转")
        suggestions.append("检查骨盆是否水平，可做仰卧屈膝左右扭转放松髋周肌群")
    elif hip_diff > 6:
        issues.append(f"左右髋关节角度略有差异（差{hip_diff:.0f}°）")
        suggestions.append("训练前进行髋关节动态拉伸，改善活动度对称性")

    # 骨盆倾斜检测（基于左右髋关键点 Y 坐标差异）
    if hip_tilt > 30:
        issues.append(f"骨盆左右严重倾斜（差{hip_tilt:.0f}px），可能伴随脊柱代偿")
        suggestions.append("评估双腿长度差异，强化弱侧臀肌，避免长期单侧负重")
    elif hip_tilt > 15:
        issues.append(f"骨盆轻微倾斜（差{hip_tilt:.0f}px），一侧髋关节略高")
        suggestions.append("站姿时两脚均匀承重，可对镜检查骨盆是否水平")

    # 躯干姿态
    # 躯干前倾/后仰角度评估
    if avg_trunk < 10:
        issues.append(f"躯干大幅前倾（{avg_trunk:.0f}°），脊柱负荷偏屈曲位，椎间盘后侧压力增大")
        suggestions.append("注意脊柱中立位，收紧核心肌群，加强胸椎灵活性训练")
    elif avg_trunk < 25:
        issues.append(f"躯干明显前倾（{avg_trunk:.0f}°），下背部承担较多负荷")
        suggestions.append("加强核心稳定性，训练时保持脊柱自然曲度")
    elif avg_trunk > 60:
        issues.append(f"躯干接近直立（{avg_trunk:.0f}°），重心偏后")
        suggestions.append("注意身体重心分配，确保足底全掌着地")
    elif avg_trunk > 45:
        issues.append(f"躯干偏直立（{avg_trunk:.0f}°），身体后链参与度可能不足")
        suggestions.append("可适当调整躯干角度，让臀肌和腘绳肌更多参与发力")

    # 关节活动度
    # 膝关节屈曲角度活动度评估
    if avg_knee < 60:
        sev = severity(60 - avg_knee, 20, 40)
        issues.append(f"膝关节大幅屈曲（{avg_knee:.0f}°），关节面压力显著增大")
        suggestions.append(f"适当减少动作幅度[{sev:.0f}%风险]，控制关节在安全范围内活动")
    elif avg_knee < 75:
        sev = severity(75 - avg_knee, 15, 30)
        issues.append(f"膝关节屈曲角度较大（{avg_knee:.0f}°），韧带负荷偏高")
        suggestions.append(f"控制动作幅度在合理范围[{sev:.0f}%风险]，训练前充分激活臀肌和腘绳肌")
    elif 75 <= avg_knee < 120:
        issues.append(f"膝关节活动范围良好（{avg_knee:.0f}°），处于功能训练黄金区间")
        suggestions.append("关节活动度理想，继续保持当前运动模式")
    elif 120 <= avg_knee < 150:
        issues.append(f"膝关节屈曲角度偏小（{avg_knee:.0f}°），关节活动范围有限")
        suggestions.append("可适当增加动作幅度，确保目标肌群获得充分刺激")
    else:
        issues.append(f"膝关节接近伸直位（{avg_knee:.0f}°），屈曲幅度很小")
        suggestions.append("如需增加训练刺激，可适当增大关节活动范围")

    # 髋关节折叠角度评估（判断髋主导发力模式）
    if avg_hip < 30:
        issues.append(f"髋关节折叠角度很小（{avg_hip:.0f}°），髋主导发力模式不足")
        suggestions.append("加强髋铰链模式训练，建立髋关节主动发力的运动习惯")
    elif avg_hip < 60:
        issues.append(f"髋关节折叠角度偏小（{avg_hip:.0f}°），膝关节可能代偿发力")
        suggestions.append("训练时先启动髋关节后移再屈膝，用臀桥和弹力带侧向行走激活臀部")

    if not suggestions:
        suggestions.append("当前姿态控制良好，继续保持规律训练！")

    return issues, suggestions


# ---------- 主分析流程：加载图片 -> 模型推理 -> 关键点提取 -> 角度计算 -> 生成报告 ----------

# 体态分析主函数：完整执行从图片读取到结果输出的全流程
def analyze_pose(image_path, llm_url=None, llm_model="auto"):
    image = cv2.imread(image_path)
    if image is None:
        print(json.dumps({"error": f"无法读取图片: {image_path}"}))
        sys.exit(1)

    h, w = image.shape[:2]

    # 加载 ONNX 推理会话（优先使用 DirectML GPU 加速，回退到 CPU）
    import onnxruntime as ort
    session = ort.InferenceSession(
        ONNX_PATH,
        providers=['DmlExecutionProvider', 'CPUExecutionProvider']
    )
    blob, scale, dx, dy = preprocess(image)
    outputs = session.run(None, {'images': blob})
    kpts_all, confs_all = postprocess(outputs, scale, dx, dy, w, h)

    # 选择置信度最高的人体检测结果
    best_idx = 0
    best_conf = 0
    for i in range(len(kpts_all)):
        conf = confs_all[i].mean()
        if conf > best_conf:
            best_conf = conf
            best_idx = i

    if best_conf < 0.1:
        print(json.dumps({"error": "未检测到人体，请确保照片中人物全身可见"}))
        sys.exit(1)

    kpts = kpts_all[best_idx]
    confs = confs_all[best_idx]

    # 提取 17 个关键点的坐标与置信度信息
    keypoints = []
    for i in range(17):
        keypoints.append({
            "x": round(float(kpts[i][0]), 2),
            "y": round(float(kpts[i][1]), 2),
            "visibility": round(float(confs[i]), 4)
        })

    def get_xy(idx):
        return (float(kpts[idx][0]), float(kpts[idx][1]))

    # 计算各主要关节角度（膝、髋、踝、躯干倾斜）
    angles = {}
    angles["left_knee"] = round(angle_between(
        get_xy(KP["LEFT_HIP"]), get_xy(KP["LEFT_KNEE"]), get_xy(KP["LEFT_ANKLE"])
    ), 1)
    angles["right_knee"] = round(angle_between(
        get_xy(KP["RIGHT_HIP"]), get_xy(KP["RIGHT_KNEE"]), get_xy(KP["RIGHT_ANKLE"])
    ), 1)
    angles["left_hip"] = round(angle_between(
        get_xy(KP["LEFT_SHOULDER"]), get_xy(KP["LEFT_HIP"]), get_xy(KP["LEFT_KNEE"])
    ), 1)
    angles["right_hip"] = round(angle_between(
        get_xy(KP["RIGHT_SHOULDER"]), get_xy(KP["RIGHT_HIP"]), get_xy(KP["RIGHT_KNEE"])
    ), 1)
    angles["left_ankle"] = round(angle_between(
        get_xy(KP["LEFT_KNEE"]), get_xy(KP["LEFT_ANKLE"]),
        (get_xy(KP["LEFT_ANKLE"])[0] + 30, get_xy(KP["LEFT_ANKLE"])[1])
    ), 1)
    angles["right_ankle"] = round(angle_between(
        get_xy(KP["RIGHT_KNEE"]), get_xy(KP["RIGHT_ANKLE"]),
        (get_xy(KP["RIGHT_ANKLE"])[0] + 30, get_xy(KP["RIGHT_ANKLE"])[1])
    ), 1)
    angles["trunk_lean_left"] = round(angle_between(
        get_xy(KP["LEFT_SHOULDER"]), get_xy(KP["LEFT_HIP"]),
        (get_xy(KP["LEFT_HIP"])[0], get_xy(KP["LEFT_HIP"])[1] - 100)
    ), 1)
    angles["trunk_lean_right"] = round(angle_between(
        get_xy(KP["RIGHT_SHOULDER"]), get_xy(KP["RIGHT_HIP"]),
        (get_xy(KP["RIGHT_HIP"])[0], get_xy(KP["RIGHT_HIP"])[1] - 100)
    ), 1)

    # 基于规则生成初步评估问题与改善建议
    issues, suggestions = generate_rule_based(kpts, angles)

    result = {
        "image_size": {"width": w, "height": h},
        "keypoints": keypoints,
        "angles": angles,
        "bones": BONES,
        "issues": issues,
        "suggestions": suggestions
    }

    # 若配置了 LLM 则调用大模型进行专业化改写
    if llm_url:
        llm_data = {"issues": result["issues"], "suggestions": result["suggestions"]}
        polished = call_llm(llm_url, llm_model, llm_data)
        if polished:
            result["issues"] = polished.get("issues", issues)
            result["suggestions"] = polished.get("suggestions", suggestions)

    print(json.dumps(result, ensure_ascii=False))


# 命令行入口：解析参数并执行体态分析
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="AI 体态分析")
    parser.add_argument("image", help="图片路径")
    parser.add_argument("--llm-url", default=None, help="LM Studio API 地址")
    parser.add_argument("--llm-model", default="auto", help="模型名称")
    args = parser.parse_args()

    analyze_pose(args.image, llm_url=args.llm_url, llm_model=args.llm_model)
