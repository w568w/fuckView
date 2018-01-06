package ml.qingsu.fuckview.ui.fragments.faq;

import java.util.ArrayList;

import ml.qingsu.fuckview.utils.faq.FaqWizard;


/**
 * Created by w568w on 2017-7-7.
 */

public class Faq extends FaqWizard {

    @Override
    protected ArrayList<faqStep> getData() {
        ArrayList<faqStep> faqSteps = new ArrayList<>();
        faqSteps.add(new faqStep(FIRST_ID, "疑难解答\n你遇到了什么问题?让我们试着逐步了解。",
                new String[]{"无法标记/屏蔽", "使用后应用卡顿", "其他"}, new int[]{FIRST_ID + 1, FIRST_ID + 16, FIRST_ID + 19}));
        faqSteps.add(new faqStep(FIRST_ID + 1, "哪里出现了问题?",
                new String[]{"无法标记", "无法屏蔽"}, new int[]{FIRST_ID + 2, FIRST_ID + 8}));
        faqSteps.add(new faqStep(FIRST_ID + 2, "标记哪个地方出了状况?",
                new String[]{"悬浮窗标记", "其他标记"}, new int[]{FIRST_ID + 3, FIRST_ID + 7}));
        faqSteps.add(new faqStep(FIRST_ID + 3, "您使用悬浮窗时遇到了什么问题?",
                new String[]{"无法弹出窗口", "刷新解析为灰色", "刷新后列表为空"}, new int[]{FIRST_ID + 4, FIRST_ID + 5, FIRST_ID + 6}));

        faqSteps.add(new faqStep(FIRST_ID + 4, "检查您是否给予 净眼 悬浮窗权限，它在一些设备中被称为\"显示系统级提示\"。"));
        faqSteps.add(new faqStep(FIRST_ID + 5, "您需要先单击\"缩小\"后才能刷新解析。"));
        faqSteps.add(new faqStep(FIRST_ID + 6, "您应当确认是否给予 净眼 Root权限。"));
        faqSteps.add(new faqStep(FIRST_ID + 7, "您应当依次确认以下项目:\n\n1.Xposed模块是否开启？\n2.更新后是否重启？\n3.应用是否事先被完全停止运行？\n提示:在选择标记应用列表中，您可以长按来快速跳转到详细信息页面。"));

        faqSteps.add(new faqStep(FIRST_ID + 8, "您使用了哪种屏蔽方式?",
                new String[]{"坐标模式", "路径模式", "经典模式"}, new int[]{FIRST_ID + 9, FIRST_ID + 13, FIRST_ID + 13}));
        faqSteps.add(new faqStep(FIRST_ID + 9, "您当时使用了哪种标记方式?",
                new String[]{"悬浮窗标记", "其他标记"}, new int[]{FIRST_ID + 10, FIRST_ID + 12}));
        faqSteps.add(new faqStep(FIRST_ID + 10, "您所标记的应用是否是全屏显示?",
                new String[]{"是", "否"}, new int[]{FIRST_ID + 11, FIRST_ID + 12}));

        faqSteps.add(new faqStep(FIRST_ID + 11, "抱歉，悬浮窗模式暂时不能准确定位全屏应用中的控件，请换用其他模式。"));
        faqSteps.add(new faqStep(FIRST_ID + 12, "您可以试着长按不能正确屏蔽的项目，选择\"设为不按类名定位\"，如果仍然不能屏蔽，尝试在设置中开启\"超强模式\"或者换用其他模式。"));

        faqSteps.add(new faqStep(FIRST_ID + 13, "您所标记的是否是抽屉菜单项、标签项或底栏按钮？",
                new String[]{"是", "否"}, new int[]{FIRST_ID + 14, FIRST_ID + 15}));

        faqSteps.add(new faqStep(FIRST_ID + 14, "屏蔽时请确保红框所标识区域并非仅包括文字和图片，还要包括周围的空白部分。\n提示:抽屉菜单项的类名通常为 NavigationMenuView ，而标签页项的类名常常与 Tab、Widget、Layout等词汇有关。"));
        faqSteps.add(new faqStep(FIRST_ID + 15, "由于无法获得更多信息，我们建议您尝试坐标模式。"));

        faqSteps.add(new faqStep(FIRST_ID + 16, "您的卡顿发生于何处？", new String[]{"所有应用", "有屏蔽项的应用"}, new int[]{FIRST_ID + 17, FIRST_ID + 18}));

        faqSteps.add(new faqStep(FIRST_ID + 17, "由于无法获得更多信息，我们猜测可能卡顿与 净眼 无关。\n排查您的手机应用、重新开关Xposed模块/框架、关闭超强模式等方法可能对您解决问题有帮助。"));

        faqSteps.add(new faqStep(FIRST_ID + 18, "关闭超强模式可能对您有帮助。"));

        faqSteps.add(new faqStep(FIRST_ID + 19, "杂项问题", new String[]{"如何标记系统应用?", "有些规则为什么无法分享?"}, new int[]{FIRST_ID + 20, FIRST_ID + 21}));
        faqSteps.add(new faqStep(FIRST_ID + 20, "长按悬浮按钮即可标记系统应用。"));
        faqSteps.add(new faqStep(FIRST_ID + 21, "由坐标模式标记的规则暂时无法分享，这是由于您的分辨率与其他设备可能不同导致的。"));
        return faqSteps;
    }
}
