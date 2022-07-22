import {FunctionComponent} from "react";
import Card from "antd/es/card/Card";

interface Props {
    docName: String,
    domain: String,
    matchCount: number
}

export const MatchPagesPanel: FunctionComponent<Props> = ({docName, domain, matchCount}) => {
    return (<Card style={{display: "flex", flexDirection: "column"}}>
        <div style={{display: "flex", flexDirection: "row"}}>
            <img src={"/files/download/title/" + domain + "/" + docName} style={{width: 250, height: "auto"}}/>
            <div style={{display: "flex", flexDirection: "column"}}>
                <h1><a target="_blank"
                       href={"/watch/" + domain + "/" + docName}>
                    {docName}</a>
                </h1>
                <h3>Домен: {domain}</h3>
                <h3>Вхождений: {matchCount}</h3>
            </div>
        </div>
    </Card>);
}