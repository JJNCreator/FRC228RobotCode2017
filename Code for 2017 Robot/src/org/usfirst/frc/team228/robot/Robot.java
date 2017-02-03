///test change
package org.usfirst.frc.team228.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DigitalInput;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
    final String defaultAuto = "Do nothing";
    final String customAuto = "Drive foward";
    String autoSelected;
    SendableChooser chooser;
    Compressor c1, c2;
    Victor v1,v2,v3,v4;
    Encoder e1,e2;
    RobotDrive officialRobot;
    Joystick baseDriverJoystick,coDriverJoystick;
    DigitalInput gearDetectionLimitSwitch;
    
    
    
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Auto_nothing", defaultAuto);
        chooser.addObject("Auto_custom", customAuto);
        SmartDashboard.putData("AutoChoices", chooser);
        
        v1 = new Victor(0);
        v2 = new Victor(1);
        v3 = new Victor(2);
        v4 = new Victor(3);
        
        officialRobot = new RobotDrive(v1, v2, v3, v4);
        
        baseDriverJoystick = new Joystick(0);
        coDriverJoystick = new Joystick(1);
    }
    
	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. If you prefer the LabVIEW
	 * Dashboard, remove all of the chooser code and uncomment the getString line to get the auto name from the text box
	 * below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
    public void autonomousInit() {
    	autoSelected = (String) chooser.getSelected();
//		autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	switch(autoSelected) {
    	case customAuto:
        //Put custom auto code here   
            break;
    	case defaultAuto:
    	default:
    	//Put default auto code here
            break;
    	}
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        officialRobot.tankDrive(baseDriverJoystick, 1, baseDriverJoystick, 5);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
