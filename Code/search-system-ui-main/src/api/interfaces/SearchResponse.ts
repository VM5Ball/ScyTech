import {QueryFlag} from "../QueryFlag";

export interface SearchResponse {
    domain: String,
    docName: String,
    score: number,
    queryFlag: QueryFlag,
}