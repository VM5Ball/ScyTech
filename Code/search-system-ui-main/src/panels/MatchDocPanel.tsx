import {FunctionComponent} from "react";
import Card from "antd/es/card/Card";
import {Button} from "antd";
import {SearchOutlined} from "@ant-design/icons";
import {DocSearchResponse} from "../api/interfaces/DocSearchResponse";

interface Props {
    match: DocSearchResponse
}

export const MatchDocPanel: FunctionComponent<Props> = ({match}) => {
    let docName = match.docName;
    let domain = match.domain;
    let title = match.title;
    const onSearchItemClick = () => {

    }
    return (<Card style={{display: "flex", flexDirection: "column"}}>
        <div style={{display: "flex", flexDirection: "row"}}>
            <img src={"/files/download/title/" + domain + "/" + docName} style={{width: 250, height: "auto"}}/>
            <div style={{display: "flex", flexDirection:"column"}}>
            <h1><a target="_blank"
                   href={"/watch/" + domain + "/" + docName}>
                {docName}</a>
            </h1>
            <h3>Домен: {domain}</h3>
            <h3>Заголовок: {title}</h3>
            </div>
        </div>
    </Card>);
}