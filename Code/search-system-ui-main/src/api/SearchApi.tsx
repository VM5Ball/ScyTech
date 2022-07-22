import {QueryFlag} from "./QueryFlag";
import {SearchResponse} from "./interfaces/SearchResponse";
import {PhraseSuggestResponse} from "./interfaces/PhraseSuggestResponse";
import {ICsrfToken} from "./interfaces/ICsrfToken";

export class SearchApi {
    token: ICsrfToken
    requestHeaders: HeadersInit

    constructor(token: ICsrfToken) {
        this.token = token;
        this.requestHeaders = new Headers();
        this.requestHeaders.set('X-CSRF-TOKEN', token.token.toString());
        this.requestHeaders.set('Content-Type', 'application/json;charset=utf-8');
    }

    suggestRequest = (phrase: String) => {
        return fetch("/suggest", {
            method: 'POST',
            headers: this.requestHeaders,
            body: JSON.stringify({
                query: phrase,
                queryFlags: QueryFlag.PHRASE
            })
        }).then(response => {
            if (response.status == 401)
                return Promise.reject("unauthorized").then(v => v)
            if (response.status == 403)
                return Promise.reject("forbidden").then(v => v)
            return response
                .json().then(
                    value => value as Array<PhraseSuggestResponse>
                );
        })
    }

    searchRequest = (phrase: String, queryFlag: QueryFlag, domains: Array<String>, files: Array<String>) => {
        return fetch("/search", {
            method: 'POST',
            headers: this.requestHeaders,
            body: JSON.stringify({
                query: phrase,
                queryFlag: queryFlag,
                domains: domains,
                files: files
            })
        }).then(response => {
            if (response.status == 401)
                return Promise.reject("unauthorized").then(v => v)
            if (response.status == 403)
                return Promise.reject("forbidden").then(v => v)
            return response
                .json().then(
                    value => value as Array<SearchResponse>
                );
        })
    }

    loadFileRequest = (domain: String, filename: String) => {
        return fetch("/files/download/" + domain + "/" + filename, {
            method: 'GET'
        }).then(response => {
            if (response.status == 401)
                return Promise.reject("unauthorized").then(v => v)
            if (response.status == 403)
                return Promise.reject("forbidden").then(v => v)
            return response.arrayBuffer();
        })
    }

    getDomains = () => {
        return fetch("/files/domains", {
            method: 'GET'
        }).then(response => {
            if (response.status == 401)
                return Promise.reject("unauthorized").then(v => v)
            if (response.status == 403)
                return Promise.reject("forbidden").then(v => v)
            return response.json().then(value => value as Array<String>);
        })
    }
}