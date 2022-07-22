import React, {useState} from 'react';
import './App.css';
import {Header} from "./Header";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {WatchPage} from "./WatchPage";
import SearchPanel from "./panels/searchPanel/SearchPanel";
import {Worker} from "@react-pdf-viewer/core";
import {ICsrfToken} from "./api/interfaces/ICsrfToken";
import {Login} from "./Login";
import {SecureApi} from "./api/SecureApi";
import {AdminPanel} from "./AdminPanel";


function App() {
    let api = new SecureApi();
    let [token, setToken] = useState<ICsrfToken | null>();
    const fetchToken = () => {
        api.getCsrfToken().then(value => setToken(value)).catch(() => setToken(null))
    }
    const onLogin = (value: FormData) => {
        api.login(value).then(value => {
            fetchToken();
        })
    }
    const onLogout = () => {
        console.log("token");
        setToken(null);
    }
    React.useEffect(() => {
        console.log("run");
        fetchToken();
    }, []);
    console.log("iasdmin", token, token?.admin);
    return (
        <Router>
            {token &&
            <Switch>
                {token.admin && <Route path={"/adminpanel"}>
                    <AdminPanel token={token}/>
                </Route>}
                <Route path="/watch/:domain/:docname">
                    <Worker workerUrl="https://unpkg.com/pdfjs-dist@2.14.305/build/pdf.worker.min.js">
                        <WatchPage/>
                    </Worker>
                </Route>
                <Route path={"/"}>
                    <Header name={token.name} onLogout={onLogout}/>
                    <SearchPanel token={token}/>
                </Route>

            </Switch>}
            {!token && <Login onLogin={onLogin}/>}
        </Router>
    );
}

export default App;
