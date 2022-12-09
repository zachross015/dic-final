docker run \
    --restart always \
    -p 7474:7474 \
    -p 7687:7687 \
    -v $PWD/$1:/data \
    -v $PWD/logs:/logs \
    -v $PWD/plugins:/plugins \
    -v $PWD/conf:/conf \
    -v $PWD/import:/import \
    -e NEO4J_AUTH=none \
    -e NEO4J_PLUGINS=\[\"apoc\"\] \
    neo4j
