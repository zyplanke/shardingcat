
package io.shardingcat.config.model;

import java.beans.Expression;

/**
 * @author shardingcat
 */
public final class TableRuleConfig {

    private final String name;
    private final RuleConfig[] rules;

    public TableRuleConfig(String name, RuleConfig[] rules) {
        this.name = name;
        this.rules = rules;
        if (rules != null) {
            for (RuleConfig r : rules) {
                r.tableRuleName = name;
            }
        }
    }

    public String getName() {
        return name;
    }

    public RuleConfig[] getRules() {
        return rules;
    }

    public static final class RuleConfig {
        private String tableRuleName;
        /** upper-case */
        private final String[] columns;
        private final Expression algorithm;

        public RuleConfig(String[] columns, Expression algorithm) {
            this.columns = columns == null ? new String[0] : columns;
            this.algorithm = algorithm;
        }

        public String[] getColumns() {
            return columns;
        }

        public Expression getAlgorithm() {
            return algorithm;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("{tableRule:").append(tableRuleName).append(", columns:[");
            for (int i = 0; i < columns.length; ++i) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(columns[i]);
            }
            s.append("]}");
            return s.toString();
        }
    }

}