import React, {FunctionComponent, useRef, useState} from 'react'
import './searchPanel.css'
import 'antd/dist/antd.css';
import Search from "antd/es/input/Search";
import {AutoComplete, Collapse, Radio, Select, Space} from "antd";
import {SearchApi} from "../../api/SearchApi";
import '@react-pdf-viewer/core/lib/styles/index.css';
import {QueryFlag} from "../../api/QueryFlag";
import {MatchPagePanel} from "../MatchPagePanel";
import {PageSearchResponse} from "../../api/interfaces/PageSearchResponse";
import {SearchResponse} from "../../api/interfaces/SearchResponse";
import {MatchDocPanel} from "../MatchDocPanel";
import {DocSearchResponse} from "../../api/interfaces/DocSearchResponse";
import {ViewMode} from "../ViewMode";
import {MatchPagesPanel} from "../MatchPagesPanel";
import {ICsrfToken} from "../../api/interfaces/ICsrfToken";

const {Option} = Select;

interface ISuggestPanel {
    value: String,
    label: JSX.Element
}

interface IFileBlob {
    domain: String,
    filename: String,
    page: String,
}

interface Props {
    token: ICsrfToken
}

const SearchPanel: FunctionComponent<Props> = ({token}) => {
    const [options, setOptions] = useState<Array<ISuggestPanel>>([]);
    const [matchItems, setMatchItems] = useState<Array<SearchResponse>>([]);
    const [viewMode, setViewMode] = useState<ViewMode>(ViewMode.PAGES);
    const [matchedElems, setMatchedElems] = useState<Array<JSX.Element>>([<></>]);
    const [searchType, setSearchType] = useState<QueryFlag>(QueryFlag.WORD)
    const [domains, setDomains] = useState<Array<JSX.Element>>(new Array<JSX.Element>());
    const fileBlob = useRef<IFileBlob | null>(null);
    const domainRef = useRef<Array<String>>([]);
    const {Panel} = Collapse;
    const searchApi = new SearchApi(token);
    let FileSaver = require('file-saver');
    const loadFile = async (domain: String, filename: String) => {
        let data = await searchApi.loadFileRequest(domain, filename);
        // TODO как - то достать mime из имени файла
        let blob = new Blob([data], {type: "application/pdf;charset=utf-8"});
        FileSaver.saveAs(blob, filename);
    }
    const handleOk = () => {
        if (fileBlob.current) {
            loadFile(fileBlob.current.domain, fileBlob.current.filename).then(() => {
            });
        }
    }
    const fetchSuggest = async (text: String) => {
        let optionsArray = [];
        let values = await searchApi.suggestRequest(text);
        console.log(values, text);
        for (let valueKey of values) {
            let suggestText = valueKey.text;
            console.log(valueKey);
            optionsArray.push({
                value: suggestText,
                label: (<><span>{suggestText}</span>
                    </>
                )
            });
        }
        setOptions(optionsArray);
    }
    const constructMatchElems = (items: Array<SearchResponse>, viewMode: ViewMode) => {
        let matchedElems = new Array();
        if (searchType == QueryFlag.WORD || searchType == QueryFlag.PHRASE) {
            switch (viewMode) {
                case ViewMode.PAGES:
                    for (let item of items) {
                        matchedElems.push(<MatchPagePanel match={item as PageSearchResponse}/>);
                    }
                    break;
                case ViewMode.DOC:
                    console.log("doc");
                    let map = new Map<String, Array<String>>();
                    for (let item of items) {
                        let page = item as PageSearchResponse;
                        let filename = page.domain + ";" + page.docName;
                        if (!map.has(filename)) {
                            map.set(filename, new Array<String>());
                        }
                        let words = [];
                        console.log(page.highlightList)
                        for (let highlight of page.highlightList) {
                            for (let word of highlight.taggedSubstrings) {
                                words.push(word.substring);
                            }
                        }
                        // @ts-ignore
                        map.set(filename, map.get(filename).concat(words));
                    }
                    map.forEach((v, k, m) => {
                        console.log("parsed", v);
                        let filename = k;
                        let parsed = filename.split(";");
                        let domain = parsed[0];
                        let docName = parsed[1];

                        matchedElems.push(<MatchPagesPanel domain={domain} docName={docName}
                                                           matchCount={v.length}/>);
                    })
                    break;
            }

        } else {
            for (let item of items) {
                matchedElems.push(<MatchDocPanel match={item as DocSearchResponse}/>);
            }
        }
        setMatchedElems(matchedElems);
    }
    const fetchSearch = async (text: String) => {
        let values = await searchApi.searchRequest(text, searchType, domainRef.current, []);
        setMatchItems(values);
        constructMatchElems(values, viewMode);
    }
    const onSearch = (value: String) => {
        fetchSearch(value).catch(e => alert(e));
    }
    const handleSearch = (value: String) => {
        fetchSuggest(value).catch();
    }
    const onSelect = (value: String) => {
        console.log('onSelect', value);
    }
    console.log(domains);
    React.useEffect(() => {
        searchApi.getDomains().then(values => {
                let elemsArr = new Array<JSX.Element>()
                for (let domain of values) {
                    // @ts-ignore
                    elemsArr.push(<Option key={domain}>{domain}</Option>)
                }
                setDomains(elemsArr);
            }
        )
    }, [])
    return (
        <section id='header' className='container'
                 style={{display: "flex", flexDirection: "column", alignItems: "center"}}>
            <div style={{display: "flex", flexDirection: "column", alignItems: "center"}}>
                <Radio.Group value={searchType} onChange={(e) => setSearchType(e.target.value)}>
                    <Radio value={QueryFlag.WORD}>По словам</Radio>
                    <Radio value={QueryFlag.PHRASE}>По фразам</Radio>
                    <Radio value={QueryFlag.TITLE}>По заголовкам документов</Radio>
                </Radio.Group>
                <AutoComplete
                    dropdownMatchSelectWidth={252}
                    style={{
                        width: "100%",
                        marginBottom: 10
                    }}
                    options={options}
                    onSelect={onSelect}
                    onSearch={handleSearch}
                >
                    <Search placeholder="Введите запрос" onSearch={onSearch} enterButton/>
                </AutoComplete>

                <Collapse style={{width: "100%"}}>
                    <Panel header="Расширенные настройки" key="1">
                        <p>Домен: </p>
                        <Select
                            mode="multiple"
                            allowClear
                            style={{
                                width: '100%',
                            }}
                            placeholder="Задайте домены"
                            onChange={(value) => {
                                domainRef.current = value;
                            }}
                        >
                            {domains}
                        </Select>
                    </Panel>
                </Collapse>
                <div style={{display: "flex", flexDirection: "row", justifyContent: "flex-end", width: "100%"}}>
                    {searchType != QueryFlag.TITLE && <>
                        <div style={{
                            margin: "0px 5px",
                            display: "flex",
                            flexDirection: "row",
                            justifyContent: "center",
                            alignItems: "center"
                        }}>группировать по:
                        </div>
                        <Select
                            defaultValue={viewMode}
                            style={{
                                width: 120,
                            }}
                            onChange={(v) => {
                                setViewMode(v);
                                constructMatchElems(matchItems, v);
                            }}>
                            <Option value={ViewMode.PAGES}>Страницам</Option>
                            <Option value={ViewMode.DOC}>Файлам</Option>
                        </Select></>}
                </div>
                <div style={{display: "flex", flexDirection: "row", justifyContent: "flex-end", width: "100%"}}>
                    {matchItems.length > 0 && <p>найдено: {matchedElems.length}</p>}
                </div>
                <Space direction="vertical">
                    {matchedElems}
                </Space>
            </div>
        </section>
    )
}

export default SearchPanel