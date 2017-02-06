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
    
    //compressor
    Compressor compressor;
    //motor controllers
	//not named based on port number, in case that changes
    Victor leftDrive1, leftDrive2, rightDrive1, rightDrive2;
    //encoders
    Encoder leftDriveEncoder, rightDriveEncoder;
    
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
        leftDrive1 = new Victor(0);
        leftDrive2 = new Victor(1);
        rightDrive1 = new Victor(2);
        rightDrive2 = new Victor(3);
        
        //Assign Robot Drive
        drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
        
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
		// can we just pass the string into the switch case in periodic? - chris
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
    	
    	//Value for the GTA Mode arcade function and SmartDashboard data
    	double combinedTriggerValue;
    	
    	switch(driveTrainId) {
    	case 0:
    		drivetrain.arcadeDrive(driverController, 1, driverController, 4);
    		break;
    	case 1:
            drivetrain.tankDrive(driverController, 1, driverController, 5);
    		break;
    	case 2:
			//the print statement would print repeatedly if run here; consider moving to init?
    		//System.out.print("GTA Mode selected"); 
			combinedTriggerValue = (-1 * driverController.getRawAxis(2) + driverController.getRawAxis(3));
    		drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
    		
			SmartDashboard.putNumber("GTADriveValue", firstArgumentValue);
			break;
    	}
    	//the code below duplicates what is already shown on the driver station?
    	SmartDashboard.putNumber("XAxisRightJoystick", driverController.getRawAxis(4));
    	SmartDashboard.putNumber("YAxisLeftJoystick", driverController.getRawAxis(1));
		
		//moved code from below into switch case code
    	/*if (driveTrainId == 2) {
    		SmartDashboard.putNumber("GTADriveValue", firstArgumentValue);
    	}*/

    	
    }
    
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
