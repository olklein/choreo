package com.olklein.choreo;

import android.content.Context;

import com.olklein.choreo.passport.CC_Blue;
import com.olklein.choreo.passport.CC_Green;
import com.olklein.choreo.passport.CC_Orange;
import com.olklein.choreo.passport.CC_Purple;
import com.olklein.choreo.passport.CC_Red;
import com.olklein.choreo.passport.CC_Yellow;
import com.olklein.choreo.passport.JV_Blue;
import com.olklein.choreo.passport.JV_Green;
import com.olklein.choreo.passport.JV_Orange;
import com.olklein.choreo.passport.JV_Purple;
import com.olklein.choreo.passport.JV_Red;
import com.olklein.choreo.passport.JV_Yellow;
import com.olklein.choreo.passport.PD_Blue;
import com.olklein.choreo.passport.PD_Purple;
import com.olklein.choreo.passport.PD_Red;
import com.olklein.choreo.passport.QS_Blue;
import com.olklein.choreo.passport.QS_Green;
import com.olklein.choreo.passport.QS_Orange;
import com.olklein.choreo.passport.QS_Purple;
import com.olklein.choreo.passport.QS_Red;
import com.olklein.choreo.passport.RB_Blue;
import com.olklein.choreo.passport.RB_Green;
import com.olklein.choreo.passport.RB_Orange;
import com.olklein.choreo.passport.RB_Purple;
import com.olklein.choreo.passport.RB_Red;
import com.olklein.choreo.passport.SB_Blue;
import com.olklein.choreo.passport.SB_Green;
import com.olklein.choreo.passport.SB_Purple;
import com.olklein.choreo.passport.SB_Red;
import com.olklein.choreo.passport.SF_Blue;
import com.olklein.choreo.passport.SF_Green;
import com.olklein.choreo.passport.SF_Purple;
import com.olklein.choreo.passport.SF_Red;
import com.olklein.choreo.passport.SW_Blue;
import com.olklein.choreo.passport.SW_Green;
import com.olklein.choreo.passport.SW_Orange;
import com.olklein.choreo.passport.SW_Purple;
import com.olklein.choreo.passport.SW_Red;
import com.olklein.choreo.passport.SW_Yellow;
import com.olklein.choreo.passport.TG_Blue;
import com.olklein.choreo.passport.TG_Green;
import com.olklein.choreo.passport.TG_Orange;
import com.olklein.choreo.passport.TG_Purple;
import com.olklein.choreo.passport.TG_Red;
import com.olklein.choreo.passport.TG_Yellow;
import com.olklein.choreo.passport.VW_Blue;
import com.olklein.choreo.passport.VW_Purple;
import com.olklein.choreo.passport.VW_Red;

import java.util.ArrayList;

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

class Passport {

    public static ArrayList<String> figures = new ArrayList<>();
    private static final ArrayList<String> figuresRhythm = new ArrayList<>();
    public static ArrayList<String[]> figuresWithTempo;
    private static int dance=0;
    private static int color=0;
    private static int discipline =0;

    private static String danceName="";

    public static void init(Context context){
        setDance(context, discipline, dance,color);
    }


    public static void setDance(Context context,int discipline, int dance, int color) {
        setDanceId(dance);
        Passport.figures= new ArrayList<>();
        Passport.figuresWithTempo= new ArrayList<>();
        int i;

        String danceStr;
        if (discipline==0) danceStr= context.getResources().getStringArray(R.array.PassportDanceS)[dance];
        else danceStr= context.getResources().getStringArray(R.array.PassportDanceL)[dance];

        String colorStr;
        colorStr = context.getResources().getStringArray(R.array.PassportColor)[color];
        if (discipline==0){
            if (dance ==2)
                colorStr = context.getResources().getStringArray(R.array.PassportColorFromPurple)[color];
            if (dance ==3)
            colorStr = context.getResources().getStringArray(R.array.PassportColorFromGreen)[color];
            if (dance ==4)
                colorStr = context.getResources().getStringArray(R.array.PassportColorFromOrange)[color];
        }
        if (discipline==1){
            if (dance ==0)
                colorStr = context.getResources().getStringArray(R.array.PassportColorFromGreen)[color];
            if (dance ==2)
                colorStr = context.getResources().getStringArray(R.array.PassportColorFromOrange)[color];
            if (dance ==3)
                colorStr = context.getResources().getStringArray(R.array.PassportColorFromPurple)[color];
        }

        String passportName=danceStr+" ("+colorStr+")";
        setDanceName(passportName);
        String[][] list;
        list = SW_Red.FIGURESTEMPOLIST;

        if (dance == 0 && discipline == 0) {
            switch (color) {
                case 0:
                    list = SW_Yellow.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = SW_Orange.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = SW_Green.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = SW_Purple.FIGURESTEMPOLIST;
                    break;
                case 4:
                    list = SW_Blue.FIGURESTEMPOLIST;
                    break;
                case 5:
                    list = SW_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = SW_Red.FIGURESTEMPOLIST;
                    break;
            }
        }
        if (dance == 1 && discipline == 0) {
            switch (color) {
                case 0:
                    list = TG_Yellow.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = TG_Orange.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = TG_Green.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = TG_Purple.FIGURESTEMPOLIST;
                    break;
                case 4:
                    list = TG_Blue.FIGURESTEMPOLIST;
                    break;
                case 5:
                    list = TG_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = TG_Red.FIGURESTEMPOLIST;
                    break;
            }
        }
        if (dance == 2 && discipline == 0) {
            switch (color) {
                case 0:
                    list = VW_Purple.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = VW_Blue.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = VW_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = VW_Red.FIGURESTEMPOLIST;
                    break;
            }
        }

        if (dance == 3 && discipline == 0) {
            switch (color) {
                case 0:
                    list = SF_Green.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = SF_Purple.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = SF_Blue.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = SF_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = SF_Red.FIGURESTEMPOLIST;
                    break;
            }
        }

        if (dance == 4 && discipline == 0) {
            switch (color) {
                case 0:
                    list = QS_Orange.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = QS_Green.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = QS_Purple.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = QS_Blue.FIGURESTEMPOLIST;
                    break;
                case 4:
                    list = QS_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = QS_Red.FIGURESTEMPOLIST;
                    break;
            }
        }


        if (dance == 0 && discipline == 1) {
            switch (color) {
                case 0:
                    list = SB_Green.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = SB_Purple.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = SB_Blue.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = SB_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = SB_Red.FIGURESTEMPOLIST;
                    break;
            }
        }

        if (dance == 1 && discipline == 1) {
            switch (color) {
                case 0:
                    list = CC_Yellow.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = CC_Orange.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = CC_Green.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = CC_Purple.FIGURESTEMPOLIST;
                    break;
                case 4:
                    list = CC_Blue.FIGURESTEMPOLIST;
                    break;
                case 5:
                    list = CC_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = TG_Red.FIGURESTEMPOLIST;
                    break;
            }
        }
        if (dance == 2 && discipline == 1) {
            switch (color) {
                case 0:
                    list = RB_Orange.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = RB_Green.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = RB_Purple.FIGURESTEMPOLIST;
                    break;
                case 3:
                    list = RB_Blue.FIGURESTEMPOLIST;
                    break;
                case 4:
                    list = RB_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = RB_Red.FIGURESTEMPOLIST;
                    break;
            }
        }
        if (dance == 3 && discipline == 1) {
            switch (color) {
                case 0:
                    list = PD_Purple.FIGURESTEMPOLIST;
                    break;
                case 1:
                    list = PD_Blue.FIGURESTEMPOLIST;
                    break;
                case 2:
                    list = PD_Red.FIGURESTEMPOLIST;
                    break;
                default:
                    list = PD_Red.FIGURESTEMPOLIST;
                    break;
            }
        }
            if (dance == 4 && discipline == 1) {
                switch (color) {
                    case 0:
                        list = JV_Yellow.FIGURESTEMPOLIST;
                        break;
                    case 1:
                        list = JV_Orange.FIGURESTEMPOLIST;
                        break;
                    case 2:
                        list = JV_Green.FIGURESTEMPOLIST;
                        break;
                    case 3:
                        list = JV_Purple.FIGURESTEMPOLIST;
                        break;
                    case 4:
                        list = JV_Blue.FIGURESTEMPOLIST;
                        break;
                    case 5:
                        list = JV_Red.FIGURESTEMPOLIST;
                        break;
                    default:
                        list = JV_Red.FIGURESTEMPOLIST;
                        break;
                }
            }

            i=0;
            while (i< list.length){
                figures.add(list[i][0]);
                figuresWithTempo.add(list[i]);
                i++;
            }
    }

    private static void setDanceName(String string) {
        danceName =string;
    }


    public static String getName() {
        return danceName;
    }
    private static void setDanceId(int id){
        dance = id;
    }
}