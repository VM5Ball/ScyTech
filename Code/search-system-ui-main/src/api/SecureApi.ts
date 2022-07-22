import {ICsrfToken} from "./interfaces/ICsrfToken";

export class SecureApi {

    getCsrfToken = () => {
        return fetch("/security/csrfToken", {
            method: 'GET'
        }).then(response => {
            if (response.status == 401)
                return Promise.reject("unauthorized").then(v => v)
            if (response.status == 403)
                return Promise.reject("forbidden").then(v => v)
            return response.json().then(value => value as ICsrfToken);
        })
    }

    login = (formData: FormData) => {
        return fetch("/login", {
            method: 'POST',
            body: formData
        }).then(value => value.text());
    }

    logout = () => {
        return fetch("/logout", {
            method: 'POST'
        }).then(value => value.text());
    }
}