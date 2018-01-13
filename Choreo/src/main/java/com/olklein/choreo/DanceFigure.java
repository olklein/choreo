package com.olklein.choreo;

/**
 * Created by olklein on 06/07/2017.
 *
 *
 *    This program is free software: you can redistribute it and/or  modify
 *    it under the terms of the GNU Affero General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *    As a special exception, the copyright holders give permission to link the
 *    code of portions of this program with the OpenSSL library under certain
 *    conditions as described in each individual source file and distribute
 *    linked combinations including the program with the OpenSSL library. You
 *    must comply with the GNU Affero General Public License in all respects
 *    for all of the code used other than as permitted herein. If you modify
 *    file(s) with this exception, you may extend this exception to your
 *    version of the file(s), but you are not obligated to do so. If you do not
 *    wish to do so, delete this exception statement from your version. If you
 *    delete this exception statement from all source files in the program,
 *    then also delete it in the license file.
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
