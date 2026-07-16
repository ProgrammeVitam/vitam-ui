#!/usr/bin/env python3
"""
Compare Spring Gateway routes between the Ansible template (.j2) and the
Java resources file. Exits 1 if predicates/filters diverge.

Usage: python3 check_gateway_sync.py
Dependencies: pip install pyyaml jinja2
"""
from __future__ import annotations

import re
import sys

import yaml
from jinja2 import Environment, FileSystemLoader, Undefined

J2_TEMPLATE_DIR = "deployment/roles/vitamui/templates/api-gateway"
J2_TEMPLATE_NAME = "application.yml.j2"
J2_TEMPLATE_FILE = f"{J2_TEMPLATE_DIR} {J2_TEMPLATE_NAME}"
JAVA_CONFIG_PATH = "api/api-gateway/src/main/resources/application-dev.yml"

# "Local dev" values matching what's hardcoded in the Java file.
# Only the variables used INSIDE the gateway.routes section need to be
# correct here (service URIs). Everything else in application.yml.j2
# (datasource, secrets, other sections...) is rendered leniently via
# SilentUndefined below.
DEV_VARS = {
    "secure": False,
    "vitamui": {
        "iam": {"host": "localhost", "port_service": 8083},
        "referential": {"host": "localhost", "port_service": 8087},
        "archive_search": {"host": "localhost", "port_service": 8089},
        "pastis": {"host": "localhost", "port_service": 8015},
        "collect": {"host": "localhost", "port_service": 8090},
        "ingest": {"host": "localhost", "port_service": 8088},
    },
}


class SilentUndefined(Undefined):
    """Any variable/attribute/filter that isn't provided becomes empty/falsy
    instead of raising. We don't want to have to declare every variable of
    the full application file (datasource, secrets...) just to compare the
    gateway.routes section."""

    def _fail_with_undefined_error(self, *args, **kwargs):
        return ""

    __str__ = lambda self: ""
    __getattr__ = lambda self, name: self
    __getitem__ = lambda self, name: self
    __bool__ = lambda self: False
    __iter__ = lambda self: iter([])


def render_template() -> dict:
    env = Environment(loader=FileSystemLoader(J2_TEMPLATE_DIR), undefined=SilentUndefined)
    # 'bool' is an Ansible filter, not available in standalone Jinja2:
    # replay it as-is. If the template uses other Ansible filters
    # (e.g. 'mandatory', 'combine'), prefer ansible.template.Templar for
    # a faithful render instead of reimplementing them one by one here.
    env.filters["bool"] = bool
    rendered = env.get_template(J2_TEMPLATE_NAME).render(**DEV_VARS)
    return yaml.safe_load(rendered)


def load_java_config() -> dict:
    with open(JAVA_CONFIG_PATH, encoding="utf-8") as f:
        return yaml.safe_load(f)


def normalize_predicate(raw: str) -> set[str]:
    """The Path=... predicate is a folded scalar (>) sensitive to YAML
    formatting. Split it into a set of patterns to compare content,
    not layout."""
    body = raw.split("Path=", 1)[-1]
    return {p.strip() for p in body.split(",") if p.strip()}


def normalize_filters(raw: list[str]) -> list[str]:
    """Filter order matters (they apply in sequence), only stray
    whitespace is normalized."""
    return [re.sub(r"\s+", " ", f).strip() for f in raw]


def _find_routes(config: dict, path: list[str]) -> list | None:
    node = config
    for key in path:
        if not isinstance(node, dict) or key not in node:
            return None
        node = node[key]
    return node


# Candidate paths depending on whether the file places the config under
# 'gateway:' directly or under the standard Spring Cloud path
# 'spring.cloud.gateway'
CANDIDATE_PATHS = [
    ["spring", "cloud", "gateway", "server", "webflux", "routes"],
    ["gateway", "server", "webflux", "routes"],
]


def extract_routes(config: dict) -> dict:
    routes = None
    for path in CANDIDATE_PATHS:
        routes = _find_routes(config, path)
        if routes is not None:
            break

    if routes is None:
        raise SystemExit(
            "Could not find the 'routes' node. Root keys found: "
            f"{list(config.keys())}. Adjust CANDIDATE_PATHS in the script."
        )

    result = {}
    for route in routes:
        predicates = route.get("predicates", [])
        result[route["id"]] = {
            "predicates": normalize_predicate(predicates[0]) if predicates else set(),
            "filters": normalize_filters(route.get("filters", [])),
        }
    return result


def diff_routes(ansible_routes: dict, java_routes: dict) -> list[str]:
    problems = []
    ansible_ids, java_ids = set(ansible_routes), set(java_routes)

    for missing in sorted(java_ids - ansible_ids):
        problems.append(f"route '{missing}' present in Java config, missing from .j2")
    for missing in sorted(ansible_ids - java_ids):
        problems.append(f"route '{missing}' present in .j2, missing from Java config")

    for route_id in sorted(ansible_ids & java_ids):
        a, j = ansible_routes[route_id], java_routes[route_id]

        only_in_ansible = a["predicates"] - j["predicates"]
        only_in_java = j["predicates"] - a["predicates"]
        if only_in_ansible:
            problems.append(f"route '{route_id}': paths only in .j2 ({J2_TEMPLATE_FILE}): {sorted(only_in_ansible)}")
        if only_in_java:
            problems.append(f"route '{route_id}': paths only in Java config ({JAVA_CONFIG_PATH}): {sorted(only_in_java)}")

        if a["filters"] != j["filters"]:
            problems.append(f"route '{route_id}': filters differ (order or content)")

    return problems


def main() -> int:
    ansible_routes = extract_routes(render_template())
    java_routes = extract_routes(load_java_config())

    problems = diff_routes(ansible_routes, java_routes)

    if problems:
        print("Drift detected between .j2 and Java resources:")
        for p in problems:
            print(f"  - {p}")
        return 1

    print("Gateway routes are in sync.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
