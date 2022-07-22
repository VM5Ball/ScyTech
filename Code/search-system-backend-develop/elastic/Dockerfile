FROM elasticsearch:7.6.0
COPY ./plugins /plugins
RUN bin/elasticsearch-plugin install https://github.com/fooger/elasticsearch-analysis-morphology/raw/master/analysis-morphology-7.6.0.zip
RUN bin/elasticsearch-plugin install --batch file:///plugins/ingest-attachment-7.6.0.zip
