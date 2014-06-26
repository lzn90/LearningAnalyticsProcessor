/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.model;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is an object that represents all configuration settings for a specific pipeline
 *
 * Each pipeline will be defined by a set of metadata which includes:
 * - name
 * - description (and recommendations for running the model)
 * - stat indicators (accuracy, confidence interval, etc.)
 * - required input fields
 * - processors (kettle ktr and kjb files, pmml files, etc.)
 * - output result definition
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class PipelineConfig {

    /**
     * the type of pipeline (e.g. marist_student_risk) this is the config for
     * (should be unique and should only use lowercase alphanums)
     */
    String type;
    String name;
    String description;

    Map<String, ?> stats;

    List<InputField> inputs;
    // TODO pipeline
    List<Output> outputs;


    // GETTERS
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, ?> getStats() {
        return stats;
    }

    public List<InputField> getInputs() {
        return inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }



    // Objects to hold specialized data

    /**
     * Represents a single field of input for a pipeline
     *
     * All inputs are defined in the resources/extracts/README.md file
     * There are 5 inputs types: PERSONAL, COURSE, ENROLLMENT, GRADE, ACTIVITY
     * A field is specified using a combination of the type and the name, for example: COURSE.COURSE_ID or PERSONAL.AGE
     */
    public static class InputField {
        public String name;
        public boolean required = false;

        private InputField() {}
        /**
         * For making input fields
         * @param name the name of the temp storage field (e.g. PERSONAL.AGE)
         * @param required true if this field is required input
         * @return the input field object
         */
        public static InputField make(String name, boolean required) {
            InputField field = new InputField();
            field.name = name;
            field.required = required;
            return field;
        }
    }

    /**
     * Defines a pipeline processor.
     * This is where all the work in the pipeline happens.
     */
    public static class Processor {
        public String name;
        public ProcessorType type;
        public String filename;

        private Processor() {}

        /**
         * Create a Pentaho Kettle based processor object
         * @param name the name of this part of the processor (mostly for logging and visuals)
         * @param filename the complete path (or relative from the pipelines directory) to the kettle ktr or kjb xml file
         * @return the processor object
         */
        public static Processor makeKettle(String name, String filename) {
            Processor obj = new Processor();
            obj.name = name;
            obj.filename = filename;
            return obj;
        }
    }

    /**
     * Represents the possible processor types
     */
    public static enum ProcessorType {
        /**
         * A Pentaho Kettle based processor
         */
        KETTLE;
        static ProcessorType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, KETTLE.name())) {
                return KETTLE;
            } else {
                throw new IllegalArgumentException("processor type ("+str+") does not match the valid types: KETTLE");
            }
        }
    }

    /**
     * Represents a type of output from a pipeline
     * The processed data from a pipeline is flushed completely after it completes
     * so the outputs allow some data to be saved
     */
    public static class Output {
        public OutputType type;
        public String from;
        public String to;
        public String filename;
        public List<OutputField> fields;

        private Output() {
            fields = new LinkedList<OutputField>();
        }

        /**
         * Create a STORAGE based output by copying
         * (copies data from temporary to persistent storage)
         * @param from the name of the Table or Collection in the temporary storage to copy from
         * @param to the name of the Table or Collection in the persistent storage to copy to
         * @return the output object
         */
        public static Output makeStorage(String from, String to) {
            Output obj = new Output();
            obj.type = OutputType.STORAGE;
            obj.from = from;
            obj.to = to;
            return obj;
        }

        /**
         * Create a STORAGE based output by copying data from temporary storage to a CSV
         * @param from the name of the Table or Collection in the temporary storage to copy from
         * @param filename the name of the CSV file to copy into
         * @return the output object
         */
        public static Output makeCSV(String from, String filename) {
            Output obj = new Output();
            obj.type = OutputType.CSV;
            obj.from = from;
            obj.filename = filename;
            return obj;
        }


        /**
         * Adds an output field to place in the persistent storage from the temporary storage
         * @param source the name of the temp storage field (e.g. AGE)
         * @param target the name of the persistent storage field (e.g. USER_ID_ALT)
         * @return the output field object
         */
        public OutputField addFieldStorage(String source, String target) {
            if (this.type != OutputType.STORAGE) {
                throw new IllegalStateException("Can only add Storage fields to a STORAGE type object, this type is: "+this.type);
            }
            OutputField field = new OutputField(this.type, source, target, null);
            this.fields.add(field);
            return field;
        }

        /**
         * Adds an output field to place in a CSV file
         * @param source the name of the temp storage field (e.g. PERSONAL.AGE)
         * @param header the name of the CSV header for this field
         * @return the output field object
         */
        public OutputField addFieldCSV(String source, String header) {
            if (this.type != OutputType.CSV) {
                throw new IllegalStateException("Can only add CSV fields to a CSV type object, this type is: "+this.type);
            }
            OutputField field = new OutputField(this.type, source, null, header);
            this.fields.add(field);
            return field;
        }
    }

    /**
     * Represents a single field of output for a pipeline
     *
     * Can output to persistent storage or a CSV file (for now)
     */
    public static class OutputField {
        public OutputType type;
        public String source;
        public String target;
        public String header;

        protected OutputField() {}

        protected OutputField(OutputType type, String source, String target, String header) {
            assert type != null;
            assert source != null;
            this.type = type;
            this.source = source;
            this.target = target;
            this.header = header;
        }
    }

    /**
     * Represents the possible output types
     */
    public static enum OutputType {
        /**
         * Output into the persistent storage
         * (tables/collections must already be defined)
         */
        STORAGE,
        /**
         * Output into a CSV file in the default location
         */
        CSV;
        static OutputType fromString(String str) {
            if (StringUtils.equalsIgnoreCase(str, STORAGE.name())) {
                return STORAGE;
            } else if (StringUtils.equalsIgnoreCase(str, CSV.name())) {
                return CSV;
            } else {
                throw new IllegalArgumentException("Output type ("+str+") does not match the valid types: CSV,STORAGE");
            }
        }
    }
}