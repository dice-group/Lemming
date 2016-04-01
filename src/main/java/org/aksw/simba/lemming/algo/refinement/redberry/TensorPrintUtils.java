package org.aksw.simba.lemming.algo.refinement.redberry;

import static cc.redberry.core.context.OutputFormat.LaTeX;
import static cc.redberry.core.context.OutputFormat.WolframMathematica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Power;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;

public class TensorPrintUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TensorPrintUtils.class);

    public static String print(Tensor tensor, OutputFormat mode) {
        StringBuilder sb = new StringBuilder();
        print(tensor, sb, mode);
        return sb.toString();
    }

    protected static void print(Tensor tensor, StringBuilder sb, OutputFormat mode) {
        if (tensor instanceof Complex) {
            sb.append(tensor.toString(mode));
            return;
        } else if (tensor instanceof SimpleTensor) {
            sb.append(tensor.toString(mode));
            return;
        } else if (tensor instanceof Sum) {
            printSum((Sum) tensor, sb, mode);
            return;
        } else if (tensor instanceof Product) {
            printProduct((Product) tensor, sb, mode);
            return;
        } else if (tensor instanceof Power) {
            printPower((Power) tensor, sb, mode);
            return;
        }
        LOGGER.error("I am missing a method to print a tensor [{}] with the class {}", tensor.toString(mode),
                tensor.getClass().getName());
    }

    protected static void printSum(Sum sum, StringBuilder sb, OutputFormat mode) {
        sb.append('(');
        print(sum.get(0), sb, mode);
        for (int i = 1; i < sum.size(); ++i) {
            sb.append('+');
            print(sum.get(i), sb, mode);
        }
        sb.append(')');
    }

    protected static void printProduct(Product product, StringBuilder sb, OutputFormat mode) {
        sb.append('(');
        char operatorChar = mode == OutputFormat.LaTeX ? ' ' : '*';
        Complex factor = product.getFactor();
        if (factor != Complex.ONE) {
            if (factor.getReal().signum() < 0) {
                sb.append('-');
                factor = factor.abs();
            }
            if (!factor.isOne()) {
                if (!factor.isZero() && !factor.isInteger()) {
                    sb.append('(');
                    sb.append(factor.toString(mode));
                    sb.append(')');
                } else {
                    sb.append(factor.toString(mode));
                }
                sb.append(operatorChar);
            }
        }

        if (product.sizeOfIndexlessPart() != product.size()) {
            LOGGER.error(
                    "This product has tensors with indexes. The current implementation is not able to print this. The product is: "
                            + product.toString(mode));
        }
        Tensor indexless[] = product.getIndexless();
        for (int i = 0; i < indexless.length; ++i) {
            if (i > 0) {
                sb.append(operatorChar);
            }
            print(indexless[i], sb, mode);
        }
        sb.append(')');
    }

    protected static void printPower(Power tensor, StringBuilder sb, OutputFormat mode) {
        Tensor argument = tensor.get(0);
        Tensor power = tensor.get(1);
        if (mode.is(WolframMathematica)) {
            sb.append('(');
            print(argument, sb, mode);
            sb.append(")^(");
            print(power, sb, mode);
            sb.append(')');
        } else if (mode.is(LaTeX)) {
            if (TensorUtils.isRealNegativeNumber(power)) {
                sb.append("\\frac{1}{");
                print(argument, sb, mode);
                if (!TensorUtils.isMinusOne(power)) {
                    sb.append("^{");
                    print(((Complex)power).abs(), sb, mode);
                    sb.append('}');
                }
                sb.append('}');
            } else {
                sb.append('(');
                print(argument, sb, mode);
                sb.append(")^{");
                print(power, sb, mode);
                sb.append('}');
            }
        } else {
            sb.append('(');
            print(argument, sb, mode);
            sb.append(")**(");
            print(power, sb, mode);
            sb.append(')');
        }
    }
}
