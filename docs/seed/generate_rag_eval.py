#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""从种子主题簇生成 RAG 检索评测集（JSONL）。

每个主题簇有 3 个措辞不同、答案一致的提问帖。对簇内每题做 leave-one-out：
用该帖的标题+正文作为问题，同簇另外两帖作为检索真值，该帖自身从检索中排除。
标准答案取该帖回复里的标准答案，用于 Judge 评答案正确性与上下文召回率。

前置条件：
    本地库已导入 docs/seed/rag_seed.sql，并导出标题→帖子 ID 映射：
    mysql -uroot -p --default-character-set=utf8mb4 -N -B software_market \
      -e "SELECT p.post_id, p.title FROM post p JOIN user u ON p.user_id=u.user_id \
          WHERE u.email LIKE 'ragseed_%' ORDER BY p.post_id" \
      > docs/seed/seed_post_ids.tsv

用法：
    python docs/seed/generate_rag_eval.py
输出：
    agent-service/src/main/resources/eval/seed-eval.jsonl
"""

from __future__ import annotations

import json
from pathlib import Path

from generate_rag_seed import TOPICS

BASE_DIR = Path(__file__).resolve().parent
OUTPUT = BASE_DIR.parent.parent / "agent-service/src/main/resources/eval/seed-eval.jsonl"
ID_MAP_FILE = BASE_DIR / "seed_post_ids.tsv"


def load_post_ids() -> dict[str, int]:
    mapping: dict[str, int] = {}
    if not ID_MAP_FILE.exists():
        return mapping
    for line in ID_MAP_FILE.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        post_id, _, title = line.partition("\t")
        mapping[title.strip()] = int(post_id)
    return mapping


def main() -> None:
    post_ids = load_post_ids()
    cases: list[dict] = []
    for cluster_index, topic in enumerate(TOPICS, start=1):
        variants = topic["variants"]
        answers = topic["answers"]
        titles = [title for title, _ in variants]
        for variant_index, (title, content) in enumerate(variants):
            other_titles = [t for i, t in enumerate(titles) if i != variant_index]
            ground_truth_ids = [post_ids[t] for t in other_titles if t in post_ids]
            case = {
                "id": f"seed-{cluster_index:02d}-{variant_index + 1}",
                "question": f"{title}\n{content}",
                "groundTruthAnswer": answers[variant_index],
                "groundTruthPostTitles": other_titles,
                "excludePostId": post_ids.get(title),
                "category": topic["category"],
            }
            if ground_truth_ids:
                case["groundTruthPostIds"] = ground_truth_ids
            cases.append(case)

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT.open("w", encoding="utf-8") as file:
        for case in cases:
            file.write(json.dumps(case, ensure_ascii=False) + "\n")
    print(f"已生成 {OUTPUT}，{len(cases)} 条用例，{len(post_ids)} 个种子帖子可映射 ID")


if __name__ == "__main__":
    main()
