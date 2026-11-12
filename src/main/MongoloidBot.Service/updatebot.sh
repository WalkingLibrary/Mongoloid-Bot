#!/usr/bin/env bash
set -euo pipefail

if [[ $(id -u) != 0 ]]; then
    echo "Run this script with sudo." >&2
    exit 1
fi
repo=$(git -C "$(dirname "$0")" rev-parse --show-toplevel)
cd "$repo"
command -v ffmpeg >/dev/null || { echo "Install FFmpeg before deploying: apt-get install ffmpeg" >&2; exit 1; }
backup=$(mktemp -d)
chmod 700 "$backup"
settings=(Options/appsettings.json Options/dev.appsettings.json)
restore_settings() {
    for name in "${settings[@]}"; do
        if [[ -f "$backup/$name" ]]; then
            cp -p "$backup/$name" "$name"
        fi
    done
}
finish() {
    restore_settings
    rm -rf -- "$backup"
}
trap finish EXIT
for name in "${settings[@]}"; do
    if [[ -f "$name" ]]; then
        mkdir -p "$backup/$(dirname "$name")"
        cp -p "$name" "$backup/$name"
    fi
    if git ls-files --error-unmatch "$name" >/dev/null 2>&1; then
        git restore --source=HEAD --worktree -- "$name"
    fi
done
# Keep server-local settings out of merges. Restore exact bytes even if pull fails.
git pull --ff-only origin master
restore_settings
bash ./gradlew test renameJar
[[ -s build/libs/mongoloidbot.jar ]]
if [[ -f mongoloidbot.jar ]]; then cp -p mongoloidbot.jar "$backup/previous.jar"; fi
systemctl stop mongoloidbot.service
install -m 644 build/libs/mongoloidbot.jar mongoloidbot.jar
systemctl start mongoloidbot.service
sleep 5
if ! systemctl is-active --quiet mongoloidbot.service; then
    if [[ -f "$backup/previous.jar" ]]; then
        cp -p "$backup/previous.jar" mongoloidbot.jar
        systemctl restart mongoloidbot.service
    fi
    echo "Updated service failed its startup check; previous JAR restored." >&2
    exit 1
fi
echo "Deployment complete: $(git rev-parse --short HEAD)"
