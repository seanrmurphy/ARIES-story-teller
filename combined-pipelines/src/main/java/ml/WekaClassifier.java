package ml;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author panc
 */

public class WekaClassifier {
    private final static Logger logger = Logger.getLogger(WekaClassifier.class.getName());

    private final String pathTrainingSet;
    private final String pathTestSet;
    private final String pathModel;

    public WekaClassifier() {
        pathTrainingSet = null;
        pathTestSet = null;
        pathModel = null;
    }

    public WekaClassifier(String pathTrainingSet, String pathTestSet, String pathModel) {
        this.pathTrainingSet = pathTrainingSet;
        this.pathTestSet = pathTestSet;
        this.pathModel = pathModel;
    }

    public void runSpecifiedMachineLearningModel(String machineLearningModel, String pathResultsPrediction) {
        String pathTrainingSet = getPathTrainingSet();
        String pathTestSet = getPathTestSet();
        String pathModel = getPathModel();

        try {
            //we create instances for training and test sets
            DataSource sourceTraining = new DataSource(pathTrainingSet);
            DataSource sourceTesting = new DataSource(pathTestSet);
            Instances train = sourceTraining.getDataSet();
            Instances test = sourceTesting.getDataSet();

            logger.info("Loading data...");

            // Set class the last attribute as class
            train.setClassIndex(train.numAttributes() - 1);
            test.setClassIndex(train.numAttributes() - 1);
            logger.info("Training data loaded");

            Classifier classifier = getClassifierClassName(machineLearningModel);
            logger.info("Classifier used: " + String.valueOf(classifier.getClass()));
            classifier.buildClassifier(train);

            Evaluation eval = new Evaluation(train);
            weka.core.SerializationHelper.write(pathModel, classifier);
            eval.evaluateModel(classifier, test);

            System.out.println("training performance results of: " + classifier.getClass().getSimpleName()
                    + "\n---------------------------------");
            System.out.println(eval.toSummaryString("\nResults", true));
            System.out.println("fmeasure: " + eval.fMeasure(1) + " Precision: " + eval.precision(1) + " Recall: " + eval.recall(1));
            System.out.println(eval.toMatrixString());
            System.out.println(eval.toClassDetailsString());
            System.out.println("AUC = " + eval.areaUnderROC(1));
            String strDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            FileWriter fileWriter = new FileWriter(pathResultsPrediction + strDate + ".txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("training performance results of: " + classifier.getClass().getSimpleName()
                    + "\n---------------------------------");
            printWriter.println(eval.toSummaryString("\nResults", true));
            printWriter.println("fmeasure: " + eval.fMeasure(1) + " Precision: " + eval.precision(1) + " Recall: " + eval.recall(1));
            printWriter.println(eval.toMatrixString());
            printWriter.println(eval.toClassDetailsString());
            printWriter.println("AUC = " + eval.areaUnderROC(1));
            printWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runSpecifiedMachineLearningModelToLabelInstances(String machineLearningModel, String pathResultsPrediction) {
        String pathTrainingSet = getPathTrainingSet();
        String pathTestSet = getPathTestSet();
        String pathModel = getPathModel();

        try {
            DataSource sourceTraining = new DataSource(pathTrainingSet);
            DataSource sourceTesting = new DataSource(pathTestSet);
            Instances train = sourceTraining.getDataSet();
            Instances test = sourceTesting.getDataSet();

            logger.info("Loading data");

            // Set class the last attribute as class
            train.setClassIndex(train.numAttributes() - 1);
            test.setClassIndex(test.numAttributes() - 1);
            logger.info("Training data loaded");

            Classifier classifier = getClassifierClassName(machineLearningModel);
            logger.info("Classifier used: "+String.valueOf(classifier.getClass()));
            logger.info("Test set items that need to be labeled:" + test.numInstances());
            logger.info("To classify such instances, consider to use the GUI version of WEKA as reported in the following example:");
            logger.info("https://github.com/spanichella/Requirement-Collector-ML-Component/blob/master/ClassifyingNewDataWeka.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runSpecifiedModelWith10FoldStrategy(String pathWholeDataset, String j48ModelPath, String machineLearningModel, String pathResultsPrediction) throws Exception {
        DataSource sourceWholeDataset = new DataSource(pathWholeDataset);
        Instances wholeDataset = sourceWholeDataset.getDataSet(); // from somewhere
        wholeDataset.setClassIndex(wholeDataset.numAttributes() - 1);

        logger.info("Loading data");
        String[] options;
        Classifier classifier = getClassifierClassName(machineLearningModel);
        logger.info("Using 10-Fold");
        logger.info("Classifier used: "+String.valueOf(classifier.getClass()));
        classifier.buildClassifier(wholeDataset);

        Evaluation eval = new Evaluation(wholeDataset);
        eval.crossValidateModel(classifier, wholeDataset, 10, new Random(1));

        System.out.println("performance results of: " + classifier.getClass().getSimpleName()
                + "\n---------------------------------");
        System.out.println(eval.toSummaryString("\nResults", true));
        System.out.println("fmeasure: " + eval.fMeasure(1) + " Precision: " + eval.precision(1) + " Recall: " + eval.recall(1));
        System.out.println(eval.toMatrixString());
        System.out.println(eval.toClassDetailsString());
        System.out.println("AUC = " + eval.areaUnderROC(1));

        String strDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        FileWriter fileWriter = new FileWriter(pathResultsPrediction + strDate + ".txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("performance results of: " + classifier.getClass().getSimpleName()
                + "\n---------------------------------");
        printWriter.println(eval.toSummaryString("\nResults", true));
        printWriter.println("fmeasure: " + eval.fMeasure(1) + " Precision: " + eval.precision(1) + " Recall: " + eval.recall(1));
        printWriter.println(eval.toMatrixString());
        printWriter.println(eval.toClassDetailsString());
        printWriter.println("AUC = " + eval.areaUnderROC(1));
        printWriter.close();
    }

    /**
     * Get classifier's class name by a short name
     */
    public static Classifier getClassifierClassName(String classifierName) {
        if (classifierName.equals("J48")) {
            return new J48();
        } else if (classifierName.equals("PART")) {
            return new PART();
        } else if (classifierName.equals("NaiveBayes")) {
            return new NaiveBayes();
        } else if (classifierName.equals("IBk")) {
            return new IBk();
        } else if (classifierName.equals("OneR")) {
            return new OneR();
        } else if (classifierName.equals("SMO")) {
            return new SMO();
        } else if (classifierName.equals("Logistic")) {
            return new Logistic();
        } else if (classifierName.equals("AdaBoostM1")) {
            return new AdaBoostM1();
        } else if (classifierName.equals("LogitBoost")) {
            return new LogitBoost();
        } else if (classifierName.equals("DecisionStump")){
            return new DecisionStump();
        } else if (classifierName.equals("LinearRegression")) {
            return new LinearRegression();
        } else if (classifierName.equals("RegressionByDiscretization")) {
            return new RegressionByDiscretization();
        } else {
            return new J48();
        }
    }

    public String getPathTrainingSet() {
        return pathTrainingSet;
    }

    public String getPathTestSet() {
        return pathTestSet;
    }

    public String getPathModel() {
        return pathModel;
    }
}
