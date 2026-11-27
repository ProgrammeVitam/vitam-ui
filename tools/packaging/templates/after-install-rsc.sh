
# Symbolic links are not followed, except a data directory linked by root to another volume
if [ -d /vitamui/data/__NAME__ ] && { [ ! -L /vitamui/data/__NAME__ ] || [ "$(stat -c %u /vitamui/data/__NAME__)" -eq 0 ]; }; then
    chown -RH __USER__:__GROUP__ /vitamui/data/__NAME__
fi

