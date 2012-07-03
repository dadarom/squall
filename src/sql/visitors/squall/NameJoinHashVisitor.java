package sql.visitors.squall;

import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.schema.Column;
import plan_runner.components.Component;
import plan_runner.conversion.TypeConversion;
import plan_runner.expressions.ColumnReference;
import plan_runner.expressions.ValueExpression;
import sql.optimizers.cost.NameTranslator;
import sql.schema.Schema;
import sql.util.NotFromMyBranchException;
import sql.util.ParserUtil;
import sql.util.TableAliasName;
import sql.util.TupleSchema;


public class NameJoinHashVisitor extends IndexJoinHashVisitor{
    private Schema _schema;
    private TableAliasName _tan;
    private NameTranslator _nt;
    private Component _affectedComponent;

    private TupleSchema _tupleSchema;

    public NameJoinHashVisitor(Schema schema, TableAliasName tan, TupleSchema tupleSchema, Component affectedComponent){
        _schema = schema;
        _tan = tan;
        _tupleSchema = tupleSchema;
        _affectedComponent = affectedComponent;
        
        _nt = new NameTranslator(affectedComponent.getName());
    }

    @Override
    public void visit(Addition adtn) {
        if(!isRecognized(adtn)){
            //normal call to parent
            super.visit(adtn);
        }
    }

    @Override
    public void visit(Multiplication m) {
        if(!isRecognized(m)){
            //normal call to parent
            super.visit(m);
        }
    }

    @Override
    public void visit(Division dvsn) {
        if(!isRecognized(dvsn)){
            //normal call to parent
            super.visit(dvsn);
        }
    }

    @Override
    public void visit(Subtraction s) {
        if(!isRecognized(s)){
            //normal call to parent
            super.visit(s);
        }
    }

    @Override
    public void visit(Function function) {
        if(!isRecognized(function)){
            //normal call to parent
            super.visit(function);
        }
    }

    @Override
    public void visit(Parenthesis prnths) {
        if(!isRecognized(prnths)){
            //normal call to parent
            super.visit(prnths);
        }
    }

    /*
     * returns true if an expression was found in tupleSchema
     *   true means no need to call parent
     * It has side effects - putting on exprStack
     */
    private <T extends Expression> boolean isRecognized(T expr){
        //expr is changed in place, so that it does not contain synonims
        int position = _nt.indexOf(_tupleSchema, expr);
        if(position != ParserUtil.NOT_FOUND){
            //we found an expression already in the tuple schema
            TypeConversion tc = _nt.getType(_tupleSchema, expr);
            ValueExpression ve = new ColumnReference(tc, position, ParserUtil.getStringExpr(expr));
            pushToExprStack(ve);
            return true;
        }else{
            return false;
        }
    }

    /*
     * only getColumnIndex method invocation is different than in parent
     */
    @Override
    public void visit(Column column) {
        String tableCompName = ParserUtil.getComponentName(column);
        List<String> ancestorNames = ParserUtil.getSourceNameList(_affectedComponent);

        if(ancestorNames.contains(tableCompName)){
            //extract type for the column
            TypeConversion tc = ParserUtil.getColumnType(column, _tan, _schema);

            //extract the position (index) of the required column
            int position = _nt.getColumnIndex(_tupleSchema, column);

            ValueExpression ve = new ColumnReference(tc, position, ParserUtil.getStringExpr(column));
            pushToExprStack(ve);
        }else{
            throw new NotFromMyBranchException();
        }
    }


}