package com.olklein.choreo;

import android.content.Context;

import com.olklein.choreo.syllabus.VienneseWaltz;
import com.olklein.choreo.syllabus.slowFox;
import com.olklein.choreo.syllabus.slowWaltz;
import com.olklein.choreo.syllabus.tango;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.olklein.choreo.R.string.slowfox;
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

class Syllabus {

    public static ArrayList<String> figures = new ArrayList<>();
    private static final ArrayList<String> figuresRhythm = new ArrayList<>();
    public static ArrayList<String[]> figuresWithTempo;
    private static int dance=R.id.allDances;
    private static String danceName="";
    private static String danceShortName="";

    public static void init(Context context){
        setDance(context,dance);
    }


    public static ArrayList<String> getRhythmFor( String figureName){

            figuresRhythm.clear();
            int i =0;
            while (i<figuresWithTempo.size()){
                if (figuresWithTempo.get(i)[0].equals(figureName)){
                    figuresRhythm.add(figuresWithTempo.get(i)[1]);
                }
                i++;
            }

        Set<String> figuresWithoutDuplicates = new LinkedHashSet<>(figuresRhythm);
        // clear the ArrayList
        figuresRhythm.clear();

        // copy elements without any duplicates
        figuresRhythm.addAll(figuresWithoutDuplicates);

        return figuresRhythm;
    }

    public static void setDance(Context context,int dance) {
        setDanceId(dance);
        Syllabus.figures= new ArrayList<>();
        Syllabus.figuresWithTempo= new ArrayList<>();
        int i;

        switch (dance) {
        case R.id.allDances:
            setDanceName(context.getResources().getString(R.string.allDances));
            setDanceShortName(context.getResources().getString(R.string.allDancesShort));
                i=0;
                while (i< slowWaltz.FIGURESTEMPOLIST.length){
                    figures.add(slowWaltz.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(slowWaltz.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< tango.FIGURESTEMPOLIST.length){
                    figures.add(tango.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(tango.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< VienneseWaltz.FIGURESTEMPOLIST.length){
                    figures.add(VienneseWaltz.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(VienneseWaltz.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< slowFox.FIGURESTEMPOLIST.length){
                    figures.add(slowFox.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(slowFox.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i]);
                    i++;
                }
                i=0;
                while (i< com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST.length){
                    figures.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i][0]);
                    figuresWithTempo.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i]);
                    i++;
                }
            break;
        case R.id.slowWaltz:
            setDanceName(context.getResources().getString(R.string.slowWaltz));
            setDanceShortName(context.getResources().getString(R.string.SW));

            i=0;
            while (i< slowWaltz.FIGURESTEMPOLIST.length){
                figures.add(slowWaltz.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.slowWaltz.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.tango:
            setDanceName(context.getResources().getString(R.string.tango));
            setDanceShortName(context.getResources().getString(R.string.TG));
            i=0;
            while (i< tango.FIGURESTEMPOLIST.length){
                figures.add(tango.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.tango.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.vienneseWaltz:
            setDanceName(context.getResources().getString(R.string.vienneseWaltz));
            setDanceShortName(context.getResources().getString(R.string.VW));
            i=0;
            while (i< VienneseWaltz.FIGURESTEMPOLIST.length){
                figures.add(VienneseWaltz.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.VienneseWaltz.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.Slowfox:
            setDanceName(context.getResources().getString(slowfox));
            setDanceShortName(context.getResources().getString(R.string.SF));
            i=0;
            while (i< slowFox.FIGURESTEMPOLIST.length){
                figures.add(slowFox.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.slowFox.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.quickstep:
            setDanceName(context.getResources().getString(R.string.quickstep));
            setDanceShortName(context.getResources().getString(R.string.QS));
            i=0;
            while (i< com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.quickstep.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.samba:
            setDanceName(context.getResources().getString(R.string.samba));
            setDanceShortName(context.getResources().getString(R.string.SB));
            i=0;
            while (i< com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.samba.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        case R.id.rumba:
            setDanceName(context.getResources().getString(R.string.rumba));
            setDanceShortName(context.getResources().getString(R.string.RB));
            i=0;
            while (i< com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.rumba.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.chacha:
            setDanceName(context.getResources().getString(R.string.chacha));
            setDanceShortName(context.getResources().getString(R.string.CC));
            i=0;
            while (i< com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.chacha.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.paso:
            setDanceName(context.getResources().getString(R.string.paso));
            setDanceShortName(context.getResources().getString(R.string.PD));
            i=0;
            while (i< com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.paso.FIGURESTEMPOLIST[i]);
                i++;
            }

            break;
        case R.id.jive:
            setDanceName(context.getResources().getString(R.string.jive));
            setDanceShortName(context.getResources().getString(R.string.JV));
            i=0;
            while (i< com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST.length){
                figures.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i][0]);
                figuresWithTempo.add(com.olklein.choreo.syllabus.jive.FIGURESTEMPOLIST[i]);
                i++;
            }
            break;
        }
        Collections.sort(figures, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });


        Comparator<String[]> comparator = new Comparator<String[]>() {
            @Override
            public int compare(String s1[], String s2[]) {
                return s1[0].compareTo(s2[0]);
            }
        };
        Collections.sort(figuresWithTempo,comparator);


        Set<String> figuresWithoutDuplicates = new LinkedHashSet<>(figures);
        // clear the ArrayList
        figures.clear();

        // copy elements but without any duplicates
        figures.addAll(figuresWithoutDuplicates);
    }

    private static void setDanceName(String string) {
        danceName =string;
    }
    private static void setDanceShortName(String string) {
        danceShortName =string;
    }

    public static String getName() {
        return danceName;
    }
    public static String getDanceShortName() {
        return danceShortName;
    }
    public static int getDanceId(){
        return dance;
    }
    private static void setDanceId(int id){
        dance = id;
    }
}