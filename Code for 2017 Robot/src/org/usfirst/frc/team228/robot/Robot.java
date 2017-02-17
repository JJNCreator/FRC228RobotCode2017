package org.usfirst.frc.team228.robot;

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
import com.ctre.CANTalon;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	//PRE INIT
	//Create variables
	
	//AUTO SELECTION
	final String defaultAuto = "Do nothing";
	final String customAuto = "Custom Auto";
	//user's autonomous selection
	String autoSelected;
	//autonomous selector
	SendableChooser autoChooser;
	
	//Checks if auto is on
	boolean inAuto; //removed "= true", because it shouldn't default to true
	
	//Count time elapsed while in autonomous
	//Timer autoTimer;
	
	//DRIVE MODE SELECTION
	//user's drive mode selection
	String driveMode;
	//Strings for each particular mode
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
	
	//Talons
	CANTalon shooterMotor1,shooterMotor2,shooterMotor3;
	
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
	//hanging variables
	boolean hangFeedForward; //when true, will apply feed forward value to hanger 
	boolean hangButtonPrev; //records state of B button from last iteration
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		//Assign chooser for Autonomous programs
		autoChooser = new SendableChooser();
		autoChooser.addDefault("Auto nothing", defaultAuto);
		autoChooser.addObject("Auto custom", customAuto);
		SmartDashboard.putData("Auto Choices", autoChooser);

		//Assign chooser for Teleop drive mode
		driveChooser = new SendableChooser();
		driveChooser.addDefault("Tank Drive", tankMode); //we need to see how these work
		driveChooser.addObject("Arcade Drive", arcadeMode);
		driveChooser.addObject("GTA", GTAMode);
		SmartDashboard.putData("Drive Choices", driveChooser);
		
		//Assign drive motor controllers
		/*
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(2);
		rightDrive2 = new VictorSP(3);
		*/
		
		//THIS IS FOR THE 2016 ROBOT ONLY:
		//COMMENT THIS OUT FOR THE NEW ROBOT
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(3);
		rightDrive2 = new VictorSP(4);
		//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		
		
		//Assign ball motor controllers
		intakeBelt = new VictorSP(7); //fuel intake //4
		feederBelt = new VictorSP(8); //shooter feed belt //5
		
		//Assign hanger motor controllers
		hangingWinch = new VictorSP(6);
		hangFeedForward = false;
		hangButtonPrev = false; 
		
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
		
		//inAuto = true;
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		switch(autoSelected) {
		case customAuto:
			//CustomAuto();
			//commenting out because we're rewriting this function
			testAuto();
		//Put custom auto code here   
			break;
		case defaultAuto:
		default:
		//Put default auto code here
			break;
		}
	}
	
	/**
	 * Jake's Autonomous Test - 2/11/17
	 */
	/*public void CustomAuto() {
		while(inAuto == true) { //While the robot is in auto mode
			
			//Have all drive motors go forward
			leftDrive1.setSpeed(1f);
			leftDrive2.setSpeed(1f);
			rightDrive1.setSpeed(-1f);
			rightDrive2.setSpeed(-1f);
			
			
			//Delay for two seconds
			Timer.delay(2f);
			
			//Resets all four drive motors
			//ResetAllMotors(); //motors don't need to be reset here
			
			
			//Have all drive motors go backward
			leftDrive1.setSpeed(-1f);
			leftDrive2.setSpeed(-1f);
			rightDrive1.setSpeed(1f);
			rightDrive2.setSpeed(1f);
			
			
			//Reset all four motors again
			ResetAllMotors();
			
			Timer.delay(0.5f);
			
			inAuto = false;
		}
	}
	
	//Commented below function out because arcadeDrive() or tankDrive() can do this same thing
	//it also isn't accurate - it only zeroes the drive motors
	private void ResetAllMotors() {
		leftDrive1.setSpeed(0.0f);
		leftDrive2.setSpeed(0.0f);
		rightDrive1.setSpeed(0.0f);
		rightDrive2.setSpeed(0.0f);

	}*/
	
	/**Haley and Chris Auto Test
	 * designed to be identical in behavior to CustomAuto 
	 * but written in a more robust way as an example for programming team
	*/
	
	public void testAuto() {
		//0s: forward
		//2s: backward
		//4s: stop
		
		//at 0s: drive forward for 2 seconds
		if (Timer.getMatchTime() < 2)
		{
			drivetrain.arcadeDrive(1.0, 0.0);
		}
		//at 2s: drive backwards for 2 seconds
		else if (Timer.getMatchTime() < 4)
		{
			drivetrain.arcadeDrive(-1.0, 0.0);
		}
		//at 4s: stop
		else
		{
			drivetrain.arcadeDrive(0.0, 0.0);
		}
	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit() {
		//get drive mode selection (tank, arcade, GTA?)
		//driveMode = (String) driveChooser.getSelected();
		//print drive mode selection
		System.out.println("Drive mode selected: " + driveMode);		
		
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
			//don't put print statements here; they would print repeatedly
			combinedTriggerValue = (-1 * driverController.getRawAxis(2) + driverController.getRawAxis(3));
			drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
			
			SmartDashboard.putNumber("GTADriveValue", combinedTriggerValue);

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
		
		//hanging
		//right trigger passes for throttle value, X button toggles feed-forward on and off
		hangingControl(operatorController.getRawAxis(3), operatorController.getXButton());
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
		feederBelt.set(-1 * intakeSpeed);
	}
	
	/** 
	* This function controls the hanging winch and feed forward
	* hangingSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	* If this is changed to a joystick, code will still work, but will allow hanger to run in reverse
	*
	* When the feed forward is enabled, hanger will stall at small voltage even without user input
	* This is designed to assist with scoring the bonus points at the end of the match
	*/
	public void hangingControl(double hangingSpeed, boolean ffButton) {
		final double hangFFValue = 0.1; //change this to change how much FF to apply
		
		//first check to see if the feed forward button has been pressed AND if it changed state
		//otherwise you would rapidly alternate between ff being on and off as long as the button was pressed!
		if (ffButton != hangButtonPrev && ffButton) { 
			//toggle the state of hang feed forward
			hangFeedForward = !hangFeedForward;
		}
		//now that check is complete, store value of button for next iteration of loop
		hangButtonPrev = ffButton;
		
		//if feed forward is on, apply it to the input
		if (hangFeedForward) {
			hangingSpeed += hangFFValue;
		}
		//if resulting value is >1.0, reduce it
		if (hangingSpeed > 1.0) {
			hangingSpeed = 1.0;
		}
		
		//actually do the hanging motor command here
		//if you need to invert this, also invert the hangFFValue constant above
		hangingWinch.set(hangingSpeed);
	}

}
