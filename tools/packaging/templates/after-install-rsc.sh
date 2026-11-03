
if [ -d /vitamui/data/__NAME__ ] && [ ! -L /vitamui/data/__NAME__ ]; then
    chown -R __USER__:__GROUP__ /vitamui/data/__NAME__
fi

