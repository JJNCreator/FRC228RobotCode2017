///test change
package org.usfirst.frc.team228.robot;
//Devins dummy test
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP; //not victor like we thought
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
	//Create variables
	
	//AUTO SELECTION
	final String defaultAuto = "Do nothing";
	final String customAuto = "Custom Auto";
	//autonomous selection
	String autoSelected;
	//autonomous selector
	SendableChooser autoChooser;
	
	//Checks if auto is on
	boolean inAuto = false;
	
	//DRIVE MODE SELECTION
	//drive mode selection
	String driveMode;
	final String arcadeMode = "Arcade";
	final String tankMode = "Tank";
	final String GTAMode = "GTA";
	
	//int driveTrainId;
	//drive mode selector
	SendableChooser driveChooser;
	
	//compressor
	Compressor compressor;
	//drive motor controllers
	VictorSP leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	//encoders
	Encoder leftDriveEncoder, rightDriveEncoder;
	
	//drive function
	RobotDrive drivetrain;
	//controllers: driver: driving, operator: functions (like intake and shooter)
	XboxController driverController,operatorController;
	
	//ball manipulation 
	//belt motor controllers
	VictorSP intakeBelt, feederBelt;
	//pneumatics
	//***insert pneumatic code here***
	//shooter controllers
	//***insert Talon code here***
	//sensors
	//***insert sensor code here***
	
	//gear manipulation
	//pneumatics
	//***insert pneumatic code here***
	//sensors
	DigitalInput gearDetectionLimitSwitch; //not currently initialized
	
	//hanging motor controllers
	VictorSP hangingWinch;
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		//Assign chooser for Autonomous programs
		autoChooser = new SendableChooser();
		autoChooser.addDefault("Auto_nothing", defaultAuto);
		autoChooser.addObject("Auto_custom", customAuto);
		SmartDashboard.putData("AutoChoices", autoChooser);

		//Assign chooser for Teleop drive mode
		driveChooser = new SendableChooser();
		driveChooser.addDefault("Tank Drive", tankMode); //we need to see how these work
		driveChooser.addObject("Arcade Drive", arcadeMode);
		driveChooser.addObject("GTA", GTAMode);
		SmartDashboard.putData("Drive Choices", driveChooser);
		
		//Assign drive motor controllers
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(2);
		rightDrive2 = new VictorSP(3);
		
		//Assign ball motor controllers
		intakeBelt = new VictorSP(4);
		feederBelt = new VictorSP(5);
		//6, 7, and 8 will be the shooter
		
		//Assign hanger motor controllers
		hangingWinch = new VictorSP(9);
		
		//Assign Robot Drive
		drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
		
		//Assign XboxControllers
		driverController = new XboxController(0);
		operatorController = new XboxController(1);
		
	}
	
	/**
	 * This autonomous (along with the chooser code above) shows how to select between different autonomous modes
	 * using the dashboard. The sendable chooser code works with the Java SmartDashboard. 
	 *
	 * You can add additional auto modes by adding additional comparisons to the switch structure below with additional strings.
	 * If using the SendableChooser make sure to add them to the chooser code above as well.
	 */
	public void autonomousInit() {
		//get Autonomous selection
		autoSelected = (String) autoChooser.getSelected();
		//print autonomous selection
		System.out.println("Auto selected: " + autoSelected);
		
		inAuto = true;
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		switch(autoSelected) {
		case customAuto:
			CustomAuto();
		//Put custom auto code here   
			break;
		case defaultAuto:
		default:
		//Put default auto code here
			break;
		}
	}
	
	/**
	 * Use for any custom behaviour in autonomous mode
	 */
	public void CustomAuto() {
		while(inAuto == true) { //While the robot is in auto mode
			
			//Have all drive motors go forward
			leftDrive1.set(0.4f);
			leftDrive2.set(0.4f);
			rightDrive1.set(0.4f);
			rightDrive2.set(0.4f);
			
			//Resets all four drive motors
			ResetAllMotors();
			
			//Delay for two seconds
			Timer.delay(2f);
			
			//Have all drive motors go backward
			leftDrive1.set(-0.4f);
			leftDrive2.set(-0.4f);
			rightDrive1.set(-0.4f);
			rightDrive2.set(-0.4f);
			
			//Reset all four motors again
			ResetAllMotors();
			
			inAuto = false;
		}
	}
	
	private void ResetAllMotors() {
		leftDrive1.set(0.0f);
		leftDrive2.set(0.0f);
		rightDrive1.set(0.0f);
		rightDrive2.set(0.0f);

	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit() {
		//get drive mode selection (tank, arcade, GTA?)
		//driveMode = (String) driveChooser.getSelected();
		//print drive mode selection
		System.out.println("Drive mode selected: " + driveMode);
		
		//Switches id based on drive mode selected
		// can we just pass the string into the switch case in periodic? - chris
		/*switch(driveMode) {
		case "Arcade":
			driveTrainId = 0; //Arcade drive
			break;
		case "Tank":
			driveTrainId = 1; //Tank drive
			break;
		case "GTA":
			driveTrainId = 2; //GTA Drive
			break;
		}*/
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {
		
		driveMode = (String)driveChooser.getSelected();
		
		//Value for the GTA Mode arcade function and SmartDashboard data
		double combinedTriggerValue;
		
		switch(driveMode) {
		case arcadeMode:
			drivetrain.arcadeDrive(driverController, 1, driverController, 4);
			break;
		case tankMode:
			drivetrain.tankDrive(driverController, 1, driverController, 5);
			break;
		case GTAMode:
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
		
		//COMMENT OUT CODE BELOW THIS IF RUNNING ON THE 2016 ROBOT
		
		//operator controls
		//ball intake
		intakeBalls(operatorController.getRawAxis(2)); //left trigger
		//ball feeding
		feedBalls(operatorController.getRawAxis(1)); //left joystick y axis
		
	}
	
	
	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
	
	}
	
	/** 
	* This function controls the belt mechanisms when feeding the shooter
	* feedSpeed is expected to be between -1 and 1
	* This allows a joystick value to be passed into it, for example
	*/
	
	public void feedBalls(double feedSpeed) {
		//intake and feeder gearboxes run in opposite directions, so one must be reversed
		//if this is backward, move -1 component to other one
		intakeBelt.set(feedSpeed);
		feederBelt.set(-1 * feedSpeed);
	}
	
	/** 
	* This function controls the belt mechanisms when picking up off the floor
	* intakeSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	*/
	public void intakeBalls(double intakeSpeed) {
		//intake should go in, feeder should go OUT 
		//since intake and feeder gearboxes run opposite, pass the SAME value to both
		intakeBelt.set(intakeSpeed);
		feederBelt.set(feedSpeed);
	}
}
