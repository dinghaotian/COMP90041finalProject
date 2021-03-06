package proj;

import proj.ethicalengine.*;
import proj.ethicalengine.Character;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @description: EthicalEngine
 * @author: Fan Jia
 */
public class EthicalEngine {

    String configPath = null;
    String resultPath = null;

    boolean collectDate = false;
    Scenario[] scenarios = null;


    public enum Decision {
        PEDESTRIANS,
        PASSENGERS;
    }

    public static void main(String[] args) throws IOException {
        EthicalEngine ethicalEngine = new EthicalEngine();
        ethicalEngine.mainProcess(args);


    }

    public void mainProcess(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--config":
                case "-c":
                    if (i != args.length - 1) {
                        configPath = args[i + 1];
                        scenarios = readConfig();
                    } else {
                        help(args);
                    }
                    break;
                case "--help":
                case "-h":
                    help(args);
                    break;
                case "--results":
                case "-r":
                    if (i != args.length - 1) {
                        resultPath = args[i + 1];
                    } else {
                        help(args);
                    }
                    break;
                case "--interactive":
                case "-i":
                    interActive(configPath != null);
                    break;
            }
        }



    }


    public Scenario[] readConfig() {
        File file = new File(configPath);
        FileReader fr;
        BufferedReader reader;
        ArrayList<Scenario> scenarios = new ArrayList<>();
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            reader.readLine();  //caption row

            int lineNumber = 0;
            ArrayList<proj.ethicalengine.Character> passenger = new ArrayList<>();
            ArrayList<proj.ethicalengine.Character> pedestrian = new ArrayList<>();
            Scenario scenario;
            String line;

            boolean legal = true;
            boolean done = false;

            while (!done) {
                if ((line = reader.readLine()) == null) {   //end reading
                    done = true;
                    Character[] passengerArray = new Character[passenger.size()];
                    for (int i1 = 0; i1 < passengerArray.length; i1++) {
                        passengerArray[i1] = passenger.get(i1);
                    }
                    Character[] pedestrianArray = new Character[pedestrian.size()];
                    for (int i1 = 0; i1 < pedestrianArray.length; i1++) {
                        pedestrianArray[i1] = pedestrian.get(i1);
                    }
                    scenario = new Scenario(passengerArray, pedestrianArray, legal);
                    scenarios.add(scenario);
                    continue;
                }

                lineNumber++;
                try {
                    String[] info = line.split(",", -1);
                    if (info.length != 10) {
                        throw new InvalidDataFormatException(lineNumber);  //skip this line and throw warning
                    }

                    if (info[0].contains("scenario:")) {                //a new scenario
                        scenario = new Scenario(legal);
                        legal = info[0].split(":")[1].contains("green");
                        if (passenger.size() != 0 && pedestrian.size() != 0) {
                            Character[] passengerArray = new Character[passenger.size()];
                            for (int i1 = 0; i1 < passengerArray.length; i1++) {
                                passengerArray[i1] = passenger.get(i1);
                            }
                            Character[] pedestrianArray = new Character[pedestrian.size()];
                            for (int i1 = 0; i1 < pedestrianArray.length; i1++) {
                                pedestrianArray[i1] = pedestrian.get(i1);
                            }

                            scenario.setPassengers(passenger);
                            scenario.setPedestrians(pedestrian);

                            scenarios.add(scenario);

                            passenger = new ArrayList<>();      //reset
                            pedestrian = new ArrayList<>();
                        }
                        continue;
                    }

                    proj.ethicalengine.Character.Gender gender;     //common characteristics
                    int age;


                    try {
                        gender = proj.ethicalengine.Character.Gender.contains(info[1]);
                    } catch (InvalidCharacteristicException e) {
                        System.out.println(e.getMessage() + lineNumber);
                        gender = proj.ethicalengine.Character.Gender.UNKNOWN;
                    }

                    try {
                        age = Integer.parseInt(info[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("WARNING: invalid number format in config file in line ");
                        age = Person.DEFAULT_AGE;
                    }


                    if (info[0].equals("person")) {     //person

                        Person.Profession profession;
                        boolean pregnant;
                        boolean isYou;
                        proj.ethicalengine.Character.BodyType bodyType;

                        try {
                            bodyType = proj.ethicalengine.Character.BodyType.contains(info[3]);
                        } catch (InvalidCharacteristicException e) {
                            System.out.println(e.getMessage() + lineNumber);
                            bodyType = proj.ethicalengine.Character.BodyType.UNSPECIFIED;
                        }

                        if (info[4].equals("")) {
                            profession = Person.Profession.NONE;
                        } else {
                            try {
                                profession = Person.Profession.contains(info[4]);
                            } catch (InvalidCharacteristicException e) {
                                System.out.println(e.getMessage() + lineNumber);
                                profession = Person.Profession.UNKNOWN;
                            }
                        }
                        pregnant = info[5].equals("yes") || info[5].equals("true");
                        isYou = info[5].equals("yes") || info[5].equals("true");
                        Person person = new Person(gender, bodyType, age, pregnant, profession);
                        if (isYou) {
                            person.setAsYou(true);
                        }
                        if (info[9].equals("passenger")) {      //as passenger
                            passenger.add(person);
                        } else {
                            pedestrian.add(person);
                        }
                    } else {        //animal
                        String specie = info[7];
                        boolean isPet;
                        if (!Animal.specie.contains(info[7])) {
                            Animal.specie.add(info[7]);     //add new specie, assume also an animal name
                        }
                        isPet = info[8].equals("yes") || info[8].equals("true");
                        Animal animal = new Animal(specie);
                        if (isPet) {
                            animal.setPet(true);
                        }
                        if (info[9].equals("passenger")) {      //as passenger
                            passenger.add(animal);
                        } else {
                            pedestrian.add(animal);
                        }
                    }
                } catch (InvalidDataFormatException e) {
                    System.out.println(e.getMessage() + lineNumber);
                }
            }

            reader.close();
            fr.close();

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: could not find config file.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Scenario[] scenariosArray = new Scenario[scenarios.size()];
        for (int i1 = 0; i1 < scenarios.size(); i1++) {
            scenariosArray[i1] = scenarios.get(i1);
        }
        return scenariosArray;
    }

    public void interActive(boolean configDate) throws IOException {
        Scanner sc = new Scanner(System.in);
        File f = new File("welcome.ascii");
        FileInputStream fis = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.US_ASCII);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        boolean done = false;
        while (!done) {
            try {
                System.out.println("Do you consent to have your decisions saved to a file? (yes/no)\n");
                String respond = sc.nextLine();
                if (respond.equals("yes")) {
                    collectDate = true;
                } else if (respond.equals("no")) {
                    collectDate = false;
                } else {
                    throw new InvalidInputException();
                }
                done = true;
            } catch (InvalidInputException e) {
                System.out.print(e.getMessage());
            }
        }

        while (true) {

            if (!configDate) {
                ScenarioGenerator generator = new ScenarioGenerator();
                scenarios = new Scenario[3];
                for (int i = 0; i < scenarios.length; i++) {
                    scenarios[i] = generator.generate();
                }
            }

            Audit audit = new Audit(scenarios);
            audit.setAuditType("User");
            int scCounter = 0;

            for (Scenario scenario : scenarios) {
                System.out.println(scenario);
                done = false;
                while (!done) {
                    try {
                        System.out.println("Who should be saved? (passenger(s) [1] or pedestrian(s) [2])");
                        String decision = sc.nextLine();
                        if (decision.equals("passenger") || decision.equals("passengers") || decision.equals("1")) {
                            audit.recordThisScenario(scenario, Decision.PASSENGERS);
                        } else if (decision.equals("pedestrian") || decision.equals("pedestrians") || decision.equals("2")) {
                            audit.recordThisScenario(scenario, Decision.PEDESTRIANS);
                        } else {
                            throw new InvalidInputException();
                        }
                        done = true;
                    } catch (InvalidInputException e) {
                        System.out.print(e.getMessage());
                    }
                }
                scCounter++;
                if (scCounter == 3) {       //after 3 scenarios
                    audit.printStatistic();
                    if (collectDate) {
                        audit.printToFile(resultPath);
                    }
                    scCounter = 0;      //reset counter

                    done = false;
                    while (!done) {
                        try {
                            System.out.println("Would you like to continue? (yes/no)");
                            String respond = sc.nextLine();
                            if (respond.equals("no")) {
                                System.exit(0);
                            } else if (!respond.equals("yes")) {
                                throw new InvalidInputException();
                            }
                            done = true;
                        } catch (InvalidInputException e) {
                            System.out.print(e.getMessage());
                        }
                    }
                    }
                }

            if (configDate) {
                audit.printStatistic();
                if (collectDate) {
                    audit.printToFile(resultPath);
                }
                System.out.println("That’s all. Press any key to quit.");
                sc.next();
                System.exit(0);

            }
        }
    }

    public void help(String[] args) {
        String help = "Usage: java EthicalEngine";
        for (String arg : args) {
            help += " " + arg;
        }
        help += "\n" + "Arguments:" + "\n" +
                " -c or --config      Optional: path to config file" + "\n" +
                " -h or --help        Print help (this message) and exit" + "\n" +
                " -r or --results     Optional: path to result log file" + "\n" +
                " -i or --interactive Optional: launches interactive mode";
        System.out.println(help);
        System.exit(0);
    }

    public static Decision decide(Scenario scenario) {
        return Decision.PASSENGERS;
    }


}
