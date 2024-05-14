package fr.gouv.vitamui.referential.internal.server.utils;

import java.util.List;

public abstract class ExportCSVParams {

    private List<String> headers;

    public List<String> getHeaders() {
        return headers;
    }

    protected void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public int getSize() {
        return headers.size();
    }

    public abstract char getSeparator();

    public abstract String getArrayJoinStr();

    public abstract String getPatternDate();
}
