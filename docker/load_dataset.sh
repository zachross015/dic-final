docker run --interactive --tty \
    -p 7474:7474 \
    -p 7687:7687 \
    -v $PWD/$1:/data \
    -v $PWD/logs:/logs \
    -v $PWD/plugins:/plugins \
    -v $PWD/conf:/conf \
    -v $PWD/import:/import \
    -e NEO4J_AUTH=none \
    -e NEO4J_PLUGINS=\[\"apoc\"\] \
    neo4j \
    neo4j-admin database import full \
    --overwrite-destination \
    --nodes=Paper=/import/papers_header.csv,/import/papers.csv \
    --nodes=Author=/import/authors_header.csv,/import/authors.csv \
    --nodes=Conference=/import/confs_header.csv,/import/confs.csv \
    --relationships=PRESENTED_AT=/import/pc_header.csv,/import/pc.csv \
    --relationships=WROTE=/import/pa_header.csv,/import/pa.csv
