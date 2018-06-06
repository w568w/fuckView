package ml.qingsu.fuckview.utils;

import android.content.Context;

import de.psdev.licensesdialog.licenses.License;

/**
 * Created by w568w on 18-5-26.
 *
 * @author w568w
 */

public class GnuAfferoGeneralPublicLicense30 extends License {
    private static final String LICENSE = "This program is free software: you can redistribute it and/or modify\n" +
            " it under the terms of the GNU Affero General Public License as\n" +
            " published by the Free Software Foundation version 3 of the\n" +
            " License.\n" +
            "\n" +
            " This program is distributed in the hope that it will be useful,\n" +
            " but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
            " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
            " GNU Affero General Public License for more details.\n" +
            "\n" +
            " You should have received a copy of the GNU Affero General Public License\n" +
            " along with this program.  If not, see <http://www.gnu.org/licenses/>.\n";

    @Override
    public String getName() {
        return "GNU Affero General Public License 3.0";
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return LICENSE;
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return LICENSE;
    }

    @Override
    public String getVersion() {
        return "3.0";
    }

    @Override
    public String getUrl() {
        return "https://www.gnu.org/licenses/";
    }
}
