package com.olklein.choreo;

/**
 * Created by olklein on 13/07/2016.
 */
class DanceFigure {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DanceFigure that = (DanceFigure) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (tempo != null ? !tempo.equals(that.tempo) : that.tempo != null) return false;
        return comment != null ? comment.equals(that.comment) : that.comment == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (tempo != null ? tempo.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public String getTempo() {
        return tempo;
    }

    public String getComment() {
        return comment;
    }

    private String name="";

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    private String tempo="";
    private String comment="";


    private long id;

    public DanceFigure(long id, String name, String tempo, String comment) {
        this.id = id;
        this.name = name+"";
        this.tempo = tempo+"";
        this.comment = comment+"";
    }

    public DanceFigure(long id, String name, String tempo) {
        setId(id);
        this.name = name;
        this.tempo = tempo;
    }


    private void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
