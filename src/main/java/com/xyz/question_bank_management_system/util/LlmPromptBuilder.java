package com.xyz.question_bank_management_system.util;

public final class LlmPromptBuilder {

    private LlmPromptBuilder() {
    }

    public static String buildQuestionAnalysisPrompt(String title,
                                                     String stem,
                                                     String standardAnswer,
                                                     String teacherAnalysis) {
        return "\u4f60\u662f C \u8bed\u8a00\u8bfe\u7a0b\u9898\u5e93\u7cfb\u7edf\u7684\u6559\u5b66\u89e3\u6790\u52a9\u624b\u3002\n"
                + "\u8bf7\u6839\u636e\u9898\u76ee\u5185\u5bb9\u751f\u6210\u4e00\u4efd\u6e05\u6670\u3001\u51c6\u786e\u3001\u9002\u5408\u5b66\u751f\u9605\u8bfb\u7684\u4e2d\u6587\u89e3\u6790\u3002\n"
                + "\u8bf7\u4e25\u683c\u9075\u5b88\u4ee5\u4e0b\u8981\u6c42\uff1a\n"
                + "1. \u5168\u7a0b\u4f7f\u7528\u4e2d\u6587\u3002\n"
                + "2. \u8f93\u51fa\u7ed3\u6784\u56fa\u5b9a\u4e3a\u4ee5\u4e0b\u56db\u4e2a\u6807\u9898\uff0c\u4e14\u6bcf\u4e2a\u6807\u9898\u90fd\u5fc5\u987b\u6709\u5185\u5bb9\uff1a\n"
                + "\u3010\u6838\u5fc3\u8003\u70b9\u3011\n"
                + "\u3010\u89e3\u9898\u601d\u8def\u3011\n"
                + "\u3010\u53c2\u8003\u89e3\u6790\u3011\n"
                + "\u3010\u6613\u9519\u63d0\u9192\u3011\n"
                + "3. \u5982\u679c\u63d0\u4f9b\u4e86\u6807\u51c6\u7b54\u6848\u6216\u6559\u5e08\u89e3\u6790\uff0c\u8bf7\u4f18\u5148\u7ed3\u5408\u5b83\u4eec\uff1b\u5982\u679c\u6ca1\u6709\uff0c\u4e5f\u8981\u57fa\u4e8e\u9898\u5e72\u7ed9\u51fa\u5408\u7406\u89e3\u6790\u3002\n"
                + "4. \u4e0d\u8981\u8f93\u51fa\u4e0e\u9898\u76ee\u65e0\u5173\u7684\u5185\u5bb9\uff0c\u4e0d\u8981\u4f7f\u7528 Markdown \u4ee3\u7801\u5757\u3002\n\n"
                + "\u9898\u76ee\u6807\u9898\uff1a" + nullToEmpty(title) + "\n"
                + "\u9898\u5e72\uff1a" + nullToEmpty(stem) + "\n"
                + "\u6807\u51c6\u7b54\u6848\uff1a" + nullToEmpty(standardAnswer) + "\n"
                + "\u6559\u5e08\u89e3\u6790\uff1a" + nullToEmpty(teacherAnalysis) + "\n";
    }

    public static String buildSubjectiveGradingPrompt(String title,
                                                      String stem,
                                                      String standardAnswer,
                                                      String analysisText,
                                                      String studentAnswer,
                                                      int maxScore,
                                                      String preferredModel,
                                                      Double preferredTemperature) {
        StringBuilder builder = new StringBuilder();
        builder.append("\u4f60\u662f C \u8bed\u8a00\u8bfe\u7a0b\u9898\u5e93\u7cfb\u7edf\u7684\u9605\u5377\u8001\u5e08\uff0c\u8bf7\u6839\u636e\u9898\u76ee\u3001\u53c2\u8003\u7b54\u6848\u548c\u89e3\u6790\u4e3a\u5b66\u751f\u7b54\u6848\u8bc4\u5206\u3002\n")
                .append("\u8bf7\u4e25\u683c\u9075\u5b88\u4ee5\u4e0b\u8981\u6c42\uff1a\n")
                .append("1. \u53ea\u8f93\u51fa JSON\uff0c\u4e0d\u8981\u8f93\u51fa\u4efb\u4f55\u989d\u5916\u8bf4\u660e\u3002\n")
                .append("2. JSON \u7ed3\u6784\u56fa\u5b9a\u4e3a\uff1a{\"\\u5206\\u6570\":number,\"\\u7f6e\\u4fe1\\u5ea6\":number,\"\\u9700\\u8981\\u590d\\u6838\":boolean,\"\\u8bc4\\u8bed\":string}\n")
                .append("3. \u201c\u5206\u6570\u201d\u5fc5\u987b\u662f 0 \u5230 ").append(maxScore).append(" \u4e4b\u95f4\u7684\u6574\u6570\u3002\n")
                .append("4. \u201c\u7f6e\u4fe1\u5ea6\u201d\u8303\u56f4\u662f 0 \u5230 1\u3002\n")
                .append("5. \u5f53\u7b54\u6848\u6a21\u7cca\u3001\u4f9d\u636e\u4e0d\u8db3\u3001\u53ef\u80fd\u9700\u8981\u4eba\u5de5\u590d\u6838\u65f6\uff0c\u201c\u9700\u8981\u590d\u6838\u201d\u8bbe\u4e3a true\u3002\n")
                .append("6. \u201c\u8bc4\u8bed\u201d\u4f7f\u7528\u4e2d\u6587\uff0c\u7b80\u8981\u8bf4\u660e\u8bc4\u5206\u4f9d\u636e\u3002\n");
        if (preferredModel != null && !preferredModel.isBlank()) {
            builder.append("7. \u672c\u6b21\u91cd\u8bd5\u4f18\u5148\u53c2\u8003\u6a21\u578b\uff1a").append(preferredModel.trim()).append("\u3002\n");
        }
        if (preferredTemperature != null) {
            builder.append("8. \u672c\u6b21\u91cd\u8bd5\u5efa\u8bae\u6e29\u5ea6\uff1a").append(preferredTemperature).append("\u3002\n");
        }
        builder.append("\n\u9898\u76ee\u6807\u9898\uff1a").append(nullToEmpty(title))
                .append("\n\u9898\u5e72\uff1a").append(nullToEmpty(stem))
                .append("\n\u53c2\u8003\u7b54\u6848\uff1a").append(nullToEmpty(standardAnswer))
                .append("\n\u89e3\u6790\uff1a").append(nullToEmpty(analysisText))
                .append("\n\u5b66\u751f\u7b54\u6848\uff1a").append(nullToEmpty(studentAnswer))
                .append("\n\u6ee1\u5206\uff1a").append(maxScore)
                .append("\n");
        return builder.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
