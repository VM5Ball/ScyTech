import React, {FunctionComponent} from "react";
import {useLocation, useParams} from "react-router-dom";
import {Viewer} from '@react-pdf-viewer/core';
import {defaultLayoutPlugin,} from '@react-pdf-viewer/default-layout';
import {searchPlugin} from '@react-pdf-viewer/search';
// Import styles
import '@react-pdf-viewer/default-layout/lib/styles/index.css';

interface IParams {
    domain: string | undefined,
    docname: string | undefined
}

function useQuery() {
    const {search} = useLocation();

    return React.useMemo(() => new URLSearchParams(search), [search]);
}

export const WatchPage: FunctionComponent = () => {
    let query = useQuery();
    let {domain, docname} = useParams<IParams>();
    let words = query.get("words")?.split(";").map(it => {
        return {keyword: it, matchCase: true}
    });
    console.log(words);
    const searchPluginInstance = searchPlugin({
        keyword: words ? words : [],
    });
    const GetPage = () => {
        if (query.get("page") !== null)
            return query.get("page");
        return 1;
    }
    const defaultLayoutPluginInstance = defaultLayoutPlugin();

    return (
        <div style={{display: "flex", flexDirection: "column", justifyContent: "center", alignItems: "center"}}>
            <div style={{height: "100vh", border: '1px solid rgba(0, 0, 0, 0.3)', width: "100%"}}>

                <Viewer
                    fileUrl={'/files/download/' + domain + '/' + docname}
                    // @ts-ignore
                    initialPage={+GetPage() - 1}
                    plugins={[defaultLayoutPluginInstance, searchPluginInstance]}
                />
            </div>
        </div>
    );
}