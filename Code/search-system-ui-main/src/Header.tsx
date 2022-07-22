import React, {FunctionComponent} from "react";
import {Typography} from "antd";
import {SecureApi} from "./api/SecureApi";

const {Title, Paragraph, Text, Link} = Typography;

interface Props {
    name: String,
    onLogout: () => void;
}

export const Header: FunctionComponent<Props> = ({name, onLogout}) => {
    const secureApi = new SecureApi();
    return (
        <div className={"Header"}>
            <Typography style={{
                display: "flex",
                flexDirection: "column",
                width: "100%",
                justifyContent: "center",
                alignItems: "center",
                padding: 10
            }}>
                <Title style={{color: "#f2eee9", margin: 0}}>Система поиска по электронному архиву</Title>
                <Title level={3}
                       style={{color: "#f2eee9", margin: 0, alignSelf: "flex-end"}}>{"Вы зашли как: " + name}</Title>
                <a style={{marginTop: 20, alignSelf: "flex-end"}} onClick={e => {
                    secureApi.logout().finally(onLogout);
                    e.preventDefault();
                }}>Выйти</a>
            </Typography>
        </div>)
}