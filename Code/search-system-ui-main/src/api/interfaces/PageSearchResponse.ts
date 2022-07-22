import {Highlight} from "./Highlight";
import {SearchResponse} from "./SearchResponse";

export interface PageSearchResponse extends SearchResponse {
    page: number,
    highlightList: Array<Highlight>
}