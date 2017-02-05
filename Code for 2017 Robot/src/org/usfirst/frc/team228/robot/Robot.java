///test change
package org.usfirst.frc.team228.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
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
	//Pre-Init
	
	//AUTO SELECTION
    final String defaultAuto = "Do nothing";
    final String customAuto = "Drive foward";
    //autonomous selection
    String autoSelected;
    //autonomous selector
    SendableChooser autoChooser;
    
    //DRIVE MODE SELECTION
    //drive mode selection
    String driveMode;
    final String arcadeMode = "Arcade";
    final String tankMode = "Tank";
    final String GTAMode = "GTA";
    
    int driveTrainId;
    //drive mode selector
    SendableChooser driveChooser;
    
    //compressors
    Compressor compressor1, compressor2;
    //motor controllers, named based on port number
    Victor leftDrive0,leftDrive1,rightDrive2,rightDrive3;
    //encoders
    Encoder encoder1,encoder2;
    
    //drive function
    RobotDrive drivetrain;
    //controllers: driver: driving, operator: functions (like intake and shooter)
    XboxController driverController,operatorController;
    //gear detection?
    DigitalInput gearDetectionLimitSwitch;
    
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	//Assign chooser for Autonomous programs
        autoChooser = new SendableChooser();
        //autoChooser.addDefault("Auto_nothing", defaultAuto);
        //autoChooser.addObject("Auto_custom", customAuto);
        //SmartDashboard.putData("AutoChoices", autoChooser);

    	//Assign chooser for Teleop drive mode
        driveChooser = new SendableChooser();
        driveChooser.addDefault("Tank Drive", tankMode); //we need to see how these work
        driveChooser.addObject("Arcade Drive", arcadeMode);
        driveChooser.addObject("GTA", GTAMode);
        SmartDashboard.putData("Drive Choices", driveChooser);
        
        //Assign motor controllers
        leftDrive0 = new Victor(0);
        leftDrive1 = new Victor(1);
        rightDrive2 = new Victor(2);
        rightDrive3 = new Victor(3);
        
        //Assign Robot Drive
        drivetrain = new RobotDrive(leftDrive0, leftDrive1, rightDrive2, rightDrive3);
        
        //Assign XboxControllers
        driverController = new XboxController(0);
        operatorController = new XboxController(1);
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
    	//get Autonomous selection
    	autoSelected = (String) autoChooser.getSelected();
		//print autonomous selection
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
    
    public void teleopInit() {
    	//teleop init
    	//get drive mode selection (tank, arcade, GTA?)
    	driveMode = (String) driveChooser.getSelected();
    	//print drive mode selection
    	System.out.println("Drive mode selected: " + driveMode);
    	
    	//Switches id based on drive mode selected
    	switch(driveMode) {
    	case "Arcade":
    		driveTrainId = 0; //Arcade drive
    		break;
    	case "Tank":
    		driveTrainId = 1; //Tank drive
    		break;
    	case "GTA":
    		driveTrainId = 2; //GTA Drive
    		break;
    	}
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
    	/**put in switch case for drive mode (tank, arcade, GTA?)
    	 */
    	
    	//initialize drive based on Id
    	if(driveTrainId == 0) {
    		drivetrain.arcadeDrive(driverController, 1, driverController, 5);
    	}
    	else if (driveTrainId == 1) {
            drivetrain.tankDrive(driverController, 1, driverController, 5);
    	}
    	else {
    		System.out.print("GTA Mode selected");
    	}
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
