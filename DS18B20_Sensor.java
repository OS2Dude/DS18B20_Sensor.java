/**
 * DS18B20_Sensor.java
 * A Java routine to read the output from multiple DS18B20 1Wire Temperature Sensors on a Rasperry Pi Zero.
 * VDD (+) Pin to 3.3v | GND (-) pin to GND | Data to GPIO 4 with 4.7k pull-up resistor to 3.3v.
 * Requires Pi4J libraries available from http://pi4j.com.
 * Based on code found in GitHub project IOT_Raspberry_Pi by WGLabz
 * Compile Command:  javac -cp .:classes:/opt/pi4j/lib/* DS18B20_Sensor.java
 * @author Chris Wolcott
 */

import java.util.ArrayList;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;
import com.pi4j.io.w1.W1Device;
import com.pi4j.temperature.TemperatureScale;

public class DS18B20_Sensor {

    private W1Master w1_Master = new W1Master();
    private ArrayList<W1Device> devices  = new ArrayList<W1Device>();

/**
 * Inner Class SensorData
 * An object to contain the Device Name & Temperature Reading for each Temperature Sensor.
 */

    private class SensorData {

        String devName;
        double devTemp;

        private SensorData(String name, double temp) {

            devName = name;
            devTemp = temp;
        }
    }

/**
 * Reads w1_master_slaves file to find devices with a Temperature Sensor prefix [28-]
 * and adds them to the ArrayList.
 * @param None
 * @return None
 */

    public void getDevices() {

        devices.clear();                // Male sure Array List is empty
        for (TemperatureSensor device : w1_Master.getDevices(TemperatureSensor.class)) {
            if (device.getName().contains("28-")) {
                devices.add((W1Device) device);
            }
        }

       return;

    }

/**
 * Reads w1_slave file from each device's directory to get its current temperature reading.
 * [/sys/bus/w1/devices/28-xxxxxxxxxxxx]  [12 digit suffix is device's serial number.]
 * @param None
 * @return double[] Temperature reading from each device.
 */

    public SensorData[] getTemperatures() {

        ArrayList<SensorData> wrkLst = new ArrayList<SensorData>();

        for (W1Device device : devices) {
            wrkLst.add(new SensorData(device.getName(), ((TemperatureSensor) device).getTemperature(TemperatureScale.CELSIUS)));
        }

        return wrkLst.toArray(new SensorData[wrkLst.size()]);

    }

/**
 * Testing method.
 * Creates list of devices (getDevices()), gets readings (getTemeratures()) and prints results.
 * @param None
 * @return None
 */

    public static void main(String[] args) {

        DS18B20_Sensor sensor = new DS18B20_Sensor();
        SensorData[] temperature;

        sensor.getDevices();

        for (int i = 0; i < 5; i++) {
            temperature = sensor.getTemperatures();
            if (temperature.length == 0) {
                System.out.println("No Temperature Sensor found.");
            }
            else {
                for (SensorData temp : temperature) {
                    System.out.print("Sensor " + temp.devName + "'s temperature reading is " + temp.devTemp + "°c");
                    System.out.println("  (" + (Math.round(((temp.devTemp * 1.8) + 32.0) * 10.0) / 10.0) + "°f)");
                }

                try {
                    Thread.sleep(60000);
                    System.out.println("");
                }
                catch (Exception x) {
                }

            }

        }

    }

}