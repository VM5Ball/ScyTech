import React, {FunctionComponent, useRef, useState} from "react";
import {Button, Modal, Select, Typography, Upload} from "antd";
import {UploadOutlined} from "@ant-design/icons";
import {SearchApi} from "./api/SearchApi";
import {ICsrfToken} from "./api/interfaces/ICsrfToken";
import {RcFile} from "antd/es/upload";

const {Title, Paragraph, Text, Link} = Typography;
const {Option} = Select;

interface Props {
    token: ICsrfToken
}

export const AdminPanel: FunctionComponent<Props> = ({token}) => {
    const [isUploadModalVisible, setUploadModalVisible] = useState<boolean>(false);
    const [domains, setDomains] = useState<Array<JSX.Element>>(new Array<JSX.Element>());
    const rcFile = useRef<RcFile>(null);
    const domainRef = useRef<String | null>(null);
    const searchApi = new SearchApi(token);
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
    const onOpenUploadModal = () => {
        setUploadModalVisible(true);
    }
    const uploadFile = () => {

    }
    // @ts-ignore
    return (<>
        <div className={"Header"}>
            <Typography style={{
                display: "flex",
                flexDirection: "column",
                width: "100%",
                justifyContent: "center",
                alignItems: "center",
                padding: 10
            }}>
                <Title style={{color: "#f2eee9", margin: 0}}>Панель администратора</Title>
            </Typography>
        </div>
        <div style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            margin: 10
        }}>
            <Button type={"primary"} style={{margin: 10}} onClick={onOpenUploadModal}>Загрузить файл</Button>
            <Button type={"primary"} style={{margin: 10}} onClick={onOpenUploadModal}>Создать домен</Button>
            <Button type={"primary"} style={{margin: 10}} onClick={onOpenUploadModal}>Выдать права на домен</Button>
            <Button type={"primary"} style={{margin: 10}} onClick={onOpenUploadModal}>Создать пользователя</Button>
            <Button type={"primary"} style={{margin: 10}} onClick={onOpenUploadModal}>Назначить роли
                пользователю</Button>
            <Modal visible={isUploadModalVisible} onOk={uploadFile} onCancel={() => {
                setUploadModalVisible(false)
            }}>
                <p>Домен: </p>
                <Select
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

                <Upload beforeUpload={(file) => {
                    //@ts-ignore
                    rcFile.current = file;
                    console.log("file")
                }} customRequest={(v) => {
                    setTimeout(() => {
                        //@ts-ignore
                        v.onSuccess("ok");
                    }, 0);
                }}>
                    <Button icon={<UploadOutlined/>}>Click to Upload</Button>
                </Upload>
            </Modal>
        </div>
    </>);
}