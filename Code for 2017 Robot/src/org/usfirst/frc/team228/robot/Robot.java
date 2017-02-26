package org.usfirst.frc.team228.robot;

import com.ctre.CANTalon; 
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
//import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.GenericHID.Hand; //probably don't need
import edu.wpi.first.wpilibj.IterativeRobot;
//import edu.wpi.first.wpilibj.Joystick; //probably don't need
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
//import edu.wpi.first.wpilibj.TalonSRX;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP; //not victor like we thought
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Preferences;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
	//PRE INIT - Create variables
	
	//ATTENTION!
	final boolean isPracticeRobot = true; //for later
	
	Preferences robotPrefs;
	
	//Auto Selection
	final String defaultAuto = "Do nothing";
	final String customAuto = "Custom Auto";
	final String gyroAuto = "Gyro Test Auto";
	//user's autonomous selection
	String autoSelected;
	//autonomous selector
	SendableChooser<String> autoChooser; //new
	//SendableChooser autoChooser; //old
	
	//Teleop Drive Mode Selection
	//Strings for each particular mode
	final String arcadeMode = "Arcade";
	final String tankMode = "Tank";
	final String GTAMode = "GTA";
	//user's drive mode selection
	String driveMode;
	//int driveTrainId;
	//drive mode selector
	SendableChooser<String> driveChooser; //old
	//SendableChooser driveChooser; //old
	
	//Compressor
	Compressor compressor;
	
	//Gyro
	ADXRS450_Gyro robotGyro;
	boolean calGyro; //true = gyro will calibrate
	
	//Drivetrain
	//drive motor controllers
	VictorSP leftDrive1, leftDrive2, rightDrive1, rightDrive2;
	//encoders
	Encoder leftDriveEncoder, rightDriveEncoder;
	//pneumatics (shifters)
	Solenoid driveShifter;
	boolean shifterLowGear, shifterButtonPrev;
		
	//Drive Function
	RobotDrive drivetrain;
	
	//XBoxControllers: driver: driving, operator: functions (like intake and shooter)
	XboxController driverController, operatorController;

	//Gear Manipulation
	//Pincher
	DoubleSolenoid pincher;
	boolean pincherClosed; //true = pinching
	boolean pincherButtonPrev; //state of the button from last iteration
	//Rotator, moves gear up/down
	Solenoid gearRotator; 
	boolean gearRotatorDown; //true = rotator down
	boolean gearRotatorButtonPrev; //state of the button from last iteration
	//sensors
	//DigitalInput gearDetectionLimitSwitch; //not currently initialized
	
	//Ball Manipulation
	//Feeder and Intake
	VictorSP intakeBelt, feederBelt; //belt motor controllers
	double feederMaxSpeed; //user input constant to control the speed of feederBelt
	//human load gate
	Solenoid HLGate;
	boolean HLGateOpen; //true = human load gate open
	boolean HLGateButtonPrev; //state of the button from last iteration
	//dumper gate
	Solenoid dumperGate;
	boolean dumperGateOpen; //true = dumper gate open
	boolean dumperButtonPrev; //state of the button from last iteration

	//Shooter
	//controllers (Talons)
	CANTalon shooterMotor1,shooterMotor2,shooterMotor3;
	boolean shooterOn;
	boolean shooterButtonPrev;
	//constant for shooter speed
	double OLShooterValue = 0.57; //"open loop shooter value" //no longer FINAL
	//target value for shooter speed, and constants for FPID
	double ShooterTarget, ShooterF, ShooterP, ShooterI, ShooterD;
	int ShooterIZone;
	//timer for how long error is below threshold
	Timer errorTimer;
	//threshold for shooter speed error - the feeder will run when under this error for a set time
	double shooterErrorThreshold;
	
	//Hanging
	//motor controllers
	VictorSP hangingWinch;
	//variables
	boolean hangFeedForward; //when true, will apply feed forward value to hanger 
	boolean hangButtonPrev; //state of the button from last iteration
	
	//Checks if auto is on
	boolean inAuto; //removed "= true", because it shouldn't default to true
	
	//Counts time elapsed while in autonomous
	Timer autoTimer;
	
	//example for toggling buttons
	//boolean exampleState; //when true, will do the thing on the robot 
	//boolean exampleButtonPrev; //state of the button from last iteration
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit()
	{
	//Robot Init - Put Things on SmartDashboard and Assign Everything
		
		//Shooter
		//put shooter constant data
		SmartDashboard.putNumber("Shooter constant", OLShooterValue);
		//get user input for constant, assign to OLShooterValue
		//puts a boolean (which becomes checkbox) on SmartDashboard
		SmartDashboard.putBoolean("Shooter PID on", false);
		//put error threshold for shooter speed to check to run feeder
		SmartDashboard.putNumber("Shooter Speed Error Threshold", shooterErrorThreshold);
		
		
		//Assign Chooser for Autonomous programs
		autoChooser = new SendableChooser<String>();
		//autoChooser = new SendableChooser();
				
		//Assign Chooser for Teleop Drive Mode
		driveChooser = new SendableChooser<String>();
		//driveChooser = new SendableChooser();
		
		//Put Autonomous Chooser
		autoChooser.addDefault("Auto nothing", defaultAuto);
		autoChooser.addObject("Auto custom", customAuto);
		autoChooser.addObject("Gyro Test Auto", gyroAuto);
		SmartDashboard.putData("Auto Choices", autoChooser);

		//Put Teleop Drive Mode Chooser
		driveChooser.addDefault("GTA Drive", GTAMode);
		driveChooser.addObject("Tank Drive", tankMode); 
		driveChooser.addObject("Arcade Drive", arcadeMode);
		SmartDashboard.putData("Drive Choices", driveChooser);

		//Autonomous mode timer
		autoTimer = new Timer();
		
		//Assign Compressor
		//compressor = new Compressor();
		
		//Assign Gyro
		robotGyro = new ADXRS450_Gyro();
		SmartDashboard.putBoolean("Gyro Calibrate", false);
		
		//Drivetrain
		//Assign drive motor controllers
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(2);
		rightDrive2 = new VictorSP(3);
		
		//Assign drivetrain shifters
		driveShifter = new Solenoid(0);
		shifterLowGear = false; //false means high gear
		shifterButtonPrev = false;
		
		//Assign drivetrain encoders
		leftDriveEncoder = new Encoder(0, 1, false); //the boolean is to indicate if it is backward
		rightDriveEncoder = new Encoder(2, 3, true); //one should always be backward of the other

		//Assign Drive Function
		drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
		
		//Assign XboxControllers
		driverController = new XboxController(0);
		operatorController = new XboxController(1);
		
		//Gear Manipulation
		//Assign Pincher
		pincher = new DoubleSolenoid(2,3);
		pincherClosed = false;
		pincherButtonPrev = false;
		//Assign Rotator
		gearRotator = new Solenoid(4);
		gearRotatorDown = false;
		gearRotatorButtonPrev = false;
		
		//Ball Manipulation
		//Intake and Feeder
		//Assign belt motor controllers
		intakeBelt = new VictorSP(4); //ball intake
		feederBelt = new VictorSP(5); //shooter feed belt
		feederMaxSpeed = 0.8; //arbitrary starting value
		SmartDashboard.putNumber("Feeder Max Speed", feederMaxSpeed); //put feederMaxSpeed on SDB
		//Assign Human Load gate
		HLGate = new Solenoid(5);
		HLGateOpen = false;
		HLGateButtonPrev = false;
		//Assign Dumper Gate
		dumperGate = new Solenoid(6);
		dumperGateOpen = false;
		dumperButtonPrev = false;
		
		//Shooter
		//Assign Shooter motor controllers
		shooterMotor1 = new CANTalon(1); //verify IDs
		shooterMotor2 = new CANTalon(2); 
		shooterMotor3 = new CANTalon(3);
		//timer for how long error is below threshold
		errorTimer = new Timer();
		
		shooterMotor3.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//set other Talons to follow - verify correct numbers for everything
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
		
		//set nominal and peak voltage, 12V means full (but only here!)
		shooterMotor3.configNominalOutputVoltage(0.0, -0.0);
		shooterMotor3.configPeakOutputVoltage(12.0, -12.0);
		if (isPracticeRobot)
		{
			shooterMotor3.reverseSensor(true);
		}
		
		//internet told me to put this here for now?
		
		//default RPM, F, P, I, D values, write these in once known
		
		//Shooter Target setting for point-blank shot, may add additional points later
		ShooterTarget = 195;
		
		//Shooter Feed Forward experimentally determined, should probably never change
		ShooterF = 3.0;
		
		//PID AND IZONE SETTINGS 
		/** Below are some workable but conservative values
		ShooterP = 3.2;
		ShooterI = 0.02;
		ShooterD = 30;
		ShooterIZone = 20; //integral error zone - ignores high error */
		
		/** These settings are a bit more aggressive. Might be slightly better than conservative settigs
		ShooterP = 5;
		ShooterI = 0.03;
		ShooterD = 100;
		ShooterIZone = 20;*/
		
		// These aggressive PID settings are what we tried last.
		// Experimentation is still needed.
		ShooterP = 10;
		ShooterI = 0.03;
		ShooterD = 260;
		ShooterIZone = 5;
		
		//display target rpm, F, P, I, and D
		SmartDashboard.putNumber("Shooter Target", ShooterTarget);
		SmartDashboard.putNumber("Shooter F", ShooterF);
		SmartDashboard.putNumber("Shooter P", ShooterP);
		SmartDashboard.putNumber("Shooter I", ShooterI);
		SmartDashboard.putNumber("Shooter D", ShooterD);
		SmartDashboard.putNumber("Shooter I Zone", ShooterIZone);
		
		shooterMotor3.setProfile(0);//what the fuck is this
		shooterMotor3.setF(ShooterF);
		shooterMotor3.setP(ShooterP);
		shooterMotor3.setI(ShooterI);
		shooterMotor3.setD(ShooterD); //the manual had these initialize to 0 but i wasn't sure
		shooterMotor3.setIZone(ShooterIZone);
		shooterButtonPrev = false;
		shooterOn = false;
		
		//Assign Hanging motor controllers
		hangingWinch = new VictorSP(6);
		hangingWinch.setInverted(true);
		hangFeedForward = false;
		hangButtonPrev = false;
		
		//set example mechanism and button statuses to false
		//exampleState = false;
		//exampleButtonPrev = false;
		
		robotPrefs = Preferences.getInstance();
	}
	
	/**
	 * This function is called each time a new packet is received from the driver station
	 * (approximately every 20ms).
	 * Periodic code for all robot modes should go here.
	 */
	public void robotPeriodic()
	{
		//Put mechanism on/off values on SDB
		SmartDashboard.putBoolean("Drivetrain Low Gear", shifterLowGear);
		SmartDashboard.putBoolean("Pincher Closed", pincherClosed);
		SmartDashboard.putBoolean("Gear Down", gearRotatorDown);
		SmartDashboard.putBoolean("Human Gate Open", HLGateOpen);
		SmartDashboard.putBoolean("Dumper Open", dumperGateOpen);
		SmartDashboard.putBoolean("Shooter", shooterOn);
		SmartDashboard.putBoolean("Winch Feed Forward", hangFeedForward);
		//put on SDB whether it's set to practice robot
		SmartDashboard.putBoolean("Practice Robot Mode", isPracticeRobot);
		
		//Get shooter constant from user on SmartDashboard
		//OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
		
		//this might be messing up SmartDashboard
		//SmartDashboard.putData("Auto Choices", autoChooser);
		//SmartDashboard.putData("Drive Choices", driveChooser);
		
		//checks if gyro calibration check box is on
		calGyro = SmartDashboard.getBoolean("Gyro Calibrate", true);

	}
	
	/**
	 * This code runs at the beginning of autonomous mode
	 */
	public void autonomousInit()
	{
		//get Autonomous selection
		autoSelected = autoChooser.getSelected();
		//print autonomous selection
		System.out.println("Auto selected: " + autoSelected);
		
		//zeros gyro. also ensures gyro finishes calibrating before continuing.
		robotGyro.reset();
		
		//reset encoders
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();
		
		//starting the timer after the gyro ensures auto runs for correct length even if delayed
		autoTimer.reset();
		autoTimer.start();
	}

	/**
	 * This function is called periodically during autonomous
	 * The switch case will determine which autonomous mode runs based on the chooser
	 */
	public void autonomousPeriodic()
	{
		switch(autoSelected)
		{
		case customAuto:
			testAuto();   
			break;
		case gyroAuto:
			gyroTestAuto();
			break;
		case defaultAuto:
		default:
			drivetrain.arcadeDrive(0.0, 0.0); //prevents watchdog error
			break;
		}
	}
	/**Haley and Chris Auto Test
	 * designed to be identical in behavior to CustomAuto 
	 * but written in a more robust way as an example for programming team
	*/
	
	public void testAuto()
	{
		//Timer.getMatchTime() wasn't working, is now autoTimer.get()
		//0s: forward
		//2s: half speed
		//4s: stop
		
		//at 0s: drive forward for 2 seconds
		if (autoTimer.get() < 2)
		{
			drivetrain.arcadeDrive(1.0, 0.0);
		}
		//at 2s: drive half speed for 2 seconds
		else if (autoTimer.get() < 4)
		{
			drivetrain.arcadeDrive(0.5, 0.0);
		}
		//at 4s: stop
		else //(autoTimer.get() > 4)
		{
			drivetrain.arcadeDrive(0.0, 0.0);
		}
	}
	
	/**
	 * This method checks the angle on the gyro repeatedly.
	 * It uses a delay, which I don't like and shouldn't be done in real code
	 */
	public void gyroTestAuto()
	{
		//turn off drivetrain
		drivetrain.arcadeDrive(0.0, 0.0);
		//check gyro
		System.out.println(robotGyro.getAngle());
		System.out.println(leftDriveEncoder.get());
		System.out.println(rightDriveEncoder.get());
		//wait .1s
		Timer.delay(0.1);
	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit()
	{
		//get drive mode selection (tank, arcade, GTA)
		//driveMode = (String) driveChooser.getSelected();
		//print drive mode selection
		//System.out.println("Drive mode selected: " + driveMode);		
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic()
	{
		//DRIVER CONTROLS
		//Value for the GTA Mode arcade function and SmartDashboard data
		double combinedTriggerValue;
		
		//Assign drive mode selection to driveMode
		driveMode = (String)driveChooser.getSelected();
		
		//Start drive mode
		switch(driveMode)
		{
		case arcadeMode:
			drivetrain.arcadeDrive(driverController, 1, driverController, 4);
			//since we aren't in GTA mode, the left bumper controls shifting
			shifterControl(driverController.getRawButton(5));
			break;
		case tankMode:
			drivetrain.tankDrive(driverController, 1, driverController, 5);
			//since we aren't in GTA mode, the left bumper controls shifting
			shifterControl(driverController.getRawButton(5));
			break;
		case GTAMode:
		default: //we needed a default case to prevent watchdog errors if smartdashboard didn't work
			combinedTriggerValue = (-1 * driverController.getRawAxis(2) + driverController.getRawAxis(3));
			drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
			//if we are in GTA mode, shifting is assigned to a face button instead
			shifterControl(driverController.getAButton());
			//display combinedTriggerValue
			//SmartDashboard.putNumber("GTADriveValue", combinedTriggerValue);
			break;
		}
		
		//the code below duplicates what is already shown on the driver station?
		//SmartDashboard.putNumber("XAxisRightJoystick", driverController.getRawAxis(4));
		//SmartDashboard.putNumber("YAxisLeftJoystick", driverController.getRawAxis(1));
		
		//OPERATOR CONTROLS
		//Pincher: toggle  with B (DoubleSolenoid.Value.kForward = closed, ...kReverse = open)
		pincherControl(operatorController.getBButton());

		//Gear Rotator: toggle with A (on/down)/(DEFAULT: off/up)
		gearRotatorControl(operatorController.getAButton());

		//Ball Intake: left trigger
		//intakeBalls(operatorController.getRawAxis(2));
		//Ball Feeding: left joystick y axis
		//feedBalls(operatorController.getRawAxis(1));
		
		//Ball intake and feed combined function, left trigger and left joystick y axis
		intakeAndFeedBalls(operatorController.getRawAxis(2), operatorController.getRawAxis(1));

		//Human Load Gate: toggle with Y (on/open)/(DEFAULT: off/closed)
		HLGateControl(operatorController.getYButton());
		
		//Dumper Gate: toggle with right bumper (6) (on/open/shoot)/(DEFAULT: off/closed/no shoot)
		dumperGateControl(operatorController.getRawButton(6));

		//Shooter: spin up/down toggle with left bumper
		shooterControl(operatorController.getRawButton(5), SmartDashboard.getBoolean("Shooter PID on", false)); //set to true for PID

		//Hanging: right trigger passes for throttle value, X button toggles feed-forward on and off
		//TEMPORARILY CHANGED TO RIGHT STICK (3 is right button)
		hangingControl(operatorController.getRawAxis(5), operatorController.getXButton());
	}
	
	public void disabledInit()
	{
		//set all of the toggle states for everything to false
		//this prevents unexpected movement on re-enable
		//pincherClosed = false; //pincherClosed should not change actually
		gearRotatorDown = false;
		HLGateOpen = false;
		dumperGateOpen = false;
		shooterOn = false;
		//shifterLowGear = false;
		//hangFeedForward = false; //also not sure here
		
		//in case the robot was in test mode, shooter motor 1 and 2 may not be in follower mode anymore
		//to be safe, they are set to follower mode every time the robot is disabled
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
	}
	
	/**
	 * This function is called periodically during disabled mode
	 */
	public void disabledPeriodic()
	{
		//if "Gyro Calibrate" is checked
		if (calGyro)
		{
			//calibrate the gyro
			robotGyro.calibrate();
		}
	}

	/**
	 * This function toggles the gear pincher open/closed using a button
	 * @param pincherButton
	 */
	public void pincherControl(boolean pincherButton)
	{
		//if the button is pressed, and if it changed state
		if (pincherButton && pincherButton != pincherButtonPrev)
		{ 
			//toggle the state of pincher
			pincherClosed = !pincherClosed;
		}
		//now that check is complete, store value of pincherButton for next iteration of loop
		pincherButtonPrev = pincherButton;

		if (pincherClosed)
		{
			//ON: pinching/closed
			pincher.set(DoubleSolenoid.Value.kForward);
		}
		else
		{
			//OFF: release/open
			pincher.set(DoubleSolenoid.Value.kReverse);
		}
	}

	/**
	 * This function toggles the gear rotator up/down using a button
	 * @param exampleButton
	 */
	public void gearRotatorControl(boolean gearRotatorButton)
	{
		//if the button is pressed, and if it changed state
		if (gearRotatorButton && gearRotatorButton != gearRotatorButtonPrev)
		{ 
			//toggle the state of gearRotator
			gearRotatorDown = !gearRotatorDown;
		}
		//now that check is complete, store value of gearRotatorButton for next iteration of loop
		gearRotatorButtonPrev = gearRotatorButton;

		if (gearRotatorDown)
		{
			//ON: gear rotator down
			gearRotator.set(true);
		}
		else
		{
			//OFF: gear rotator up
			gearRotator.set(false);
		}
	}	
	
	/** 
	* This function controls the belt mechanisms when picking up off the floor
	* intakeSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	*/
	
	//This function did not work when feedBalls was run in the same loop
	//They would overwrite the commands of each other and it caused problems
	//new combined function located below
	/*
	public void intakeBalls(double intakeSpeed)
	{
		//intake should go in, feeder should go OUT 
		//since intake and feeder gearboxes run opposite, pass the SAME value to both
		intakeBelt.set(intakeSpeed);
		feederBelt.set(-1 * intakeSpeed);
	}
	*/
	/** 
	* This function controls the belt mechanisms when feeding the shooter
	* feedSpeed is expected to be between -1 and 1
	* This allows a joystick value to be passed into it, for example
	* @param feedSpeed
	*/
	/*
	public void feedBalls(double feedSpeed)
	{
		//intake and feeder gearboxes run in opposite directions, so one must be reversed
		//if this is backward, move -1 component to other one
		intakeBelt.set(-1 * feedSpeed);
		feederBelt.set(-1 * feedSpeed);
	}
	*/
	/**
	* This function is a copy of both the intake and feed functions, put together.
	* This prevents conflicts between the two.
	* When you get time later, overload this function with a one-constructor version
	* intakeSpeed and feedSpeed are values based on the joystick input
	* for intake and feeding respectively
	*/
	public void intakeAndFeedBalls(double intakeSpeed, double feedSpeed)
	{
		//get user input for Feeder Max Speed
		feederMaxSpeed = SmartDashboard.getNumber("Feeder Max Speed", feederMaxSpeed);	
		
		//get error threshold for shooter speed to check to run feeder
		shooterErrorThreshold = SmartDashboard.getNumber("Shooter Speed Error Threshold", shooterErrorThreshold);
		
		//eliminates deadband caused by cheapo xbox controllers
		if (intakeSpeed > -0.1 && intakeSpeed < 0.1) 
		{
			intakeSpeed = 0;
		}
		if (feedSpeed > -0.1 && feedSpeed < 0.1)
		{
			feedSpeed = 0;
		}
		
		//run the intake
		intakeBelt.set(intakeSpeed + (-1 * feedSpeed));
		
		//run the feeder
		//if PID is on
		if (SmartDashboard.getBoolean("Shooter PID on", false))
		{
			//if shooter is stable at correct speed
			if (mechanismStable(shooterMotor3.getClosedLoopError() < shooterErrorThreshold
					&& shooterMotor3.getClosedLoopError() > (shooterErrorThreshold*-1)))
			{
				//run the feeder
				feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);
			}
			else
				//don't run feeder
				feederBelt.set(0);
		}
		//if not PID
		else
		{
			//run the feeder
			feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);
		}

	}

	/**
	 * This function toggles the human load gate using a button
	 * @param HLGateButton
	 */
	public void HLGateControl(boolean HLGateButton)
	{
		//if the button is pressed, and if it changed state
		if (HLGateButton && HLGateButton != HLGateButtonPrev)
		{ 
			//toggle the state of HLGate
			HLGateOpen = !HLGateOpen;
		}
		//now that check is complete, store value of HLGateButton for next iteration of loop
		HLGateButtonPrev = HLGateButton;

		if (HLGateOpen)
		{
			//ON: open/no shoot state
			HLGate.set(true);
		}
		else
		{
			//OFF: closed/shoot state
			HLGate.set(false);
		}
	}

	/**
	 * This function toggles the dumper gate using a button
	 * @param dumperButton
	 */
	public void dumperGateControl(boolean dumperButton)
	{
		//if the button is pressed, and if it changed state
		if (dumperButton && dumperButton != dumperButtonPrev)
		{ 
			//toggle the state of dumperGate
			dumperGateOpen = !dumperGateOpen;
		}
		//now that check is complete, store value of dumperButton for next iteration of loop
		dumperButtonPrev = dumperButton;

		if (dumperGateOpen)
		{
			//dumperGate open/no shoot state
			dumperGate.set(true);
		}
		else
		{
			//dumperGate closed/shoot state
			dumperGate.set(false);
		}
	}
	
	/**
	 * This function toggles the drivetrain shifters using a button
	 * We will probably also call this in autonomous
	 * @param shifterButton
	 */
	
	public void shifterControl(boolean shifterButton)
	{
		//if button is pressed and the state changed (button isn't being held)
		if (shifterButton && shifterButton != shifterButtonPrev) 
		{
			//toggle the shifter state (false means high gear!)
			shifterLowGear = !shifterLowGear;
		}
		//now that the check is complete, store button value for next loop
		shifterButtonPrev = shifterButton;
		
		if (shifterLowGear)
		{
			//set to low gear
			driveShifter.set(true);
		} 
		else 
		{
			//set to high gear
			driveShifter.set(false);
		}
	}
	
	/**
	 * This function handles the shooter motors on the 2017 robot.
	 * @param shooterButton, isPID
	 */
	public void shooterControl(boolean shooterButton, boolean isPID)
	{
		
		//Graph Signal and Error on SmartDashboard. Display Speed as number
		SmartDashboard.putNumber("Shooter Signal", shooterMotor3.getOutputVoltage() / shooterMotor3.getBusVoltage());
		SmartDashboard.putNumber("Shooter Error", shooterMotor3.getClosedLoopError());
		SmartDashboard.putNumber("Shooter Speed", shooterMotor3.getSpeed());
		//move back if needed to inside PID code
		
		//get user input for constant, assign to OLShooterValue
		OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
		
		//if a new button press has started, turn shooter on/off
		//toggle shooter
		if(shooterButton && shooterButton != shooterButtonPrev)
		{
			shooterOn = !shooterOn;
		}
		
		shooterButtonPrev = shooterButton;
		
		if (shooterOn) //if the shooter has toggled on
		{
			//SmartDashboard.putBoolean("Shooter On", true); //indicate on dash that shooter is on //this is now in robotPeriodic
			if (isPID) //if the PID check box on smartdashboard is on
			{
				//get latest constants from SmartDashboard
				ShooterTarget = SmartDashboard.getNumber("Shooter Target", ShooterTarget);
				ShooterF = SmartDashboard.getNumber("Shooter F", ShooterF);
				ShooterP = SmartDashboard.getNumber("Shooter P", ShooterP);
				ShooterI = SmartDashboard.getNumber("Shooter I", ShooterI);
				ShooterD = SmartDashboard.getNumber("Shooter D", ShooterD);
				ShooterIZone = (int) SmartDashboard.getNumber("Shooter I Zone", ShooterIZone);
				
				//double ShooterTarget = 4000.0; //now a global variable
				shooterMotor3.changeControlMode(TalonControlMode.Speed);
				//encoder is 12 CPR (counts per revolution)
				//speed setpoint is ticks per 10ms

				//set PID loop to values from smart dashboard
				shooterMotor3.set(ShooterTarget);
				shooterMotor3.setF(ShooterF);
				shooterMotor3.setP(ShooterP);
				shooterMotor3.setI(ShooterI);
				shooterMotor3.setD(ShooterD);
				shooterMotor3.setIZone(ShooterIZone);
				
			}
			else //if PID is unchecked
			{
				//change talon to open-loop mode, set a value
				shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);
				shooterMotor3.set(OLShooterValue); //this value is from SmartDashboard
			}
		}
		else
		{
			//change talon to open-loop mode, set to off
			//SmartDashboard.putBoolean("Shooter On", false); //like above, this is now in robotPeriodic
			shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);
			shooterMotor3.set(0.0);
		}
	}
	
	/**This method will take a boolean (that is, if the absolute value error of the shooter
	 * is under a certain threshold), and if true (that is, the error is under that threshold),
	 * it will start a timer. If the timer reaches a certain amount of time (indicating the shooter
	 * is stable at the desired speed), the method will return true. If the timer has not reached
	 * that time, the method will return false. If the input boolean is false (that is, the error is
	 * over the threshold), the timer will stop, then reset, and the method will return false.
	 * 
	 * the return value of the method will be used to determine whether the shooter feed will run.
	 * @param isLowError
	 * @return
	 */
	public boolean mechanismStable(boolean isLowError)
	{
		//if the error is below threshold
		if (isLowError)
		{
			//if the timer has not started (it will be 0)
			if (errorTimer.get() == 0.0)
			{
				errorTimer.start();
			}
			//if timer is under 0.25s
			if (errorTimer.get() > 0.25)
			{
				//run feeder
				return true;
			}
			//if the error is above threshold and timer is not < 0.25s
			else
			{
				//don't run feeder
				return false;
			}
		}
		//if the error is not below threshold
		else
		{
			//stop the timer
			errorTimer.stop();
			//reset the timer
			errorTimer.reset();
			//run the feeder
			return false;
		}
		
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
	public void hangingControl(double hangingSpeed, boolean ffButton)
	{
		final double hangFFValue = 0.3; //change this to change how much FF to apply
		
		//first check to see if the feed forward button has been pressed AND if it changed state
		//otherwise you would rapidly alternate between ff being on and off as long as the button was pressed!
		if (ffButton != hangButtonPrev && ffButton) 
		{ 
			//toggle the state of hang feed forward
			hangFeedForward = !hangFeedForward;
		}
		//now that check is complete, store value of button for next iteration of loop
		hangButtonPrev = ffButton;
		
		//if feed forward is on, apply it to the input
		if (hangFeedForward) 
		{
			hangingSpeed += hangFFValue;
		}
		//if resulting value is >1.0, reduce it
		if (hangingSpeed > 1.0) 
		{
			hangingSpeed = 1.0;
		}
		
		//actually do the hanging motor command here
		//if you need to invert this, also invert the hangFFValue constant above
		hangingWinch.set(hangingSpeed);
	}
	
	/** Test mode is used in order to verify motors are working properly and spinning in correct direction.
	* Do not use for anything other than debugging!!
	*/
	
	//Currently Test mode is only used to make sure all 3 shooter motors are independently working
	
	public void testInit() {
		//change motors 1 and 2 back to percent voltage mode
		shooterMotor1.changeControlMode(TalonControlMode.PercentVbus);
		shooterMotor2.changeControlMode(TalonControlMode.PercentVbus);
	}
	
	public void testPeriodic() {
		if (operatorController.getAButton())
		{
			shooterMotor1.set(1.0);
		}
		else
			shooterMotor1.set(0);
		if (operatorController.getBButton())
		{
			shooterMotor2.set(1.0);
		}
		else
		{
			shooterMotor2.set(0);
		}
		if (operatorController.getXButton())
		{
			shooterMotor3.set(1.0);
		}
		else
		{
			shooterMotor3.set(0);
		}
	}
	
	/**
	 * This is an example function for toggling when you press a button
	 * 
	 * It takes the the value of whatever button you want to use
	 * and it does the thing with the mechanism.
	 * 
	 * create global variables boolean exampleState, exampleButtonPrev in pre-init
	 * set exampleState, exampleButtonPrev to beginning state in robot init, typically false
	 * 
	 * to call this method:
	 * exampleMechanismControl(operatorController.getZButton());
	 */
	/*
	public void exampleMechanismControl(boolean exampleButton)
	{
		//if the button is pressed, and if it changed state
		if (exampleButton && exampleButton != exampleButtonPrev)
		{ 
			//toggle the state of mechanism
			exampleState = !exampleState;
		}
		//now that check is complete, store value of exampleButton for next iteration of loop
		exampleButtonPrev = exampleButton;

		if (exampleState)
		{
			//mechanism ON state
			exampleMechanism.set(true);
		}
		else
		{
			//mechanism OFF state
			exampleMechanism.set(false);
		}
	}
	*/
}