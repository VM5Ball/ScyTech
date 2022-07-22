import {FunctionComponent} from "react";
import {PageSearchResponse} from "../api/interfaces/PageSearchResponse";
import Card from "antd/es/card/Card";

interface Props {
    match: PageSearchResponse
}

export const MatchPagePanel: FunctionComponent<Props> = ({match}) => {
    let docName = match.docName;
    let domain = match.domain;
    let page = match.page;
    let highlights = match.highlightList;
    let highLightsElems = []
    let words = [];
    for (let highlight of highlights) {
        let startIndex = 0;
        let accString = "";
        let sourceString = highlight.highlightString;
        let taggedStrings = highlight.taggedSubstrings
        for (let tag of taggedStrings) {
            let start = tag.start;
            let end = tag.end;
            let left = sourceString.substring(startIndex, start);
            accString += left + "<b style='background: yellow'>" + tag.substring + "</b>";
            startIndex = end;
            if (words.indexOf(tag.substring) == -1)
                words.push(tag.substring);
        }
        accString += sourceString.substring(startIndex);
        highLightsElems.push(<span style={{margin: 5, borderBottom: "0.05rem solid", width: "100%"}}
                                   dangerouslySetInnerHTML={{__html: accString}}/>);
    }
    const onSearchItemClick = () => {

    }
    if (words.length == 0)
        return (null);
    return (<Card style={{display: "flex", flexDirection: "column"}}>
        <h1><a target="_blank"
            href={"/watch/" + domain + "/" + docName + "?page=" + page + "&words=" + words.reduce((a, b) => a + ";" + b)}>
            {docName}
        </a>
        </h1>
        <h3>Домен: {domain}</h3>
        <h3>Страница: {page}</h3>
        <div style={{display: "flex", flexDirection: "column"}}>{highLightsElems}</div>
        {/*<div><Button type="primary" icon={<SearchOutlined/>} size='large' onClick={onSearchItemClick}/></div>*/}
    </Card>);
}