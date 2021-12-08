// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.can.BaseTalon;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

import frc.robot.Constants;
import frc.robot.Constants.DriveConstants;

public class DriveSubsystem extends SubsystemBase {
  /** Creates a new DriveSubsystem. */

  private WPI_TalonFX[] rightDriveTalonFX = new WPI_TalonFX[DriveConstants.rightMotorPortID.length];
  private WPI_TalonFX[] leftDriveTalonFX = new WPI_TalonFX[DriveConstants.leftMotorPortID.length];
  public DifferentialDrive drive;

  public DriveSubsystem() {

    /**
     * create objects for the left-side and right-side motors reset controllers to
     * defaults setup followers set controller orientation set encoder phase
     */
    for (int motor = 0; motor < DriveConstants.rightMotorPortID.length; motor++) {
      rightDriveTalonFX[motor] = new WPI_TalonFX(DriveConstants.rightMotorPortID[motor]);
      rightDriveTalonFX[motor].configFactoryDefault(); // reset the controller to defaults

      if (motor == 0) { // setup master
        rightDriveTalonFX[motor].set(ControlMode.PercentOutput, 0); // set the motor to Percent Output with Default of 0
        rightDriveTalonFX[motor].setInverted(true); // right side will be inverted
      } else { // setup followers
        rightDriveTalonFX[motor].follow(rightDriveTalonFX[0]);
        rightDriveTalonFX[motor].setInverted(InvertType.FollowMaster); // set green lights when going forward
      }
    }

    for (int motor = 0; motor < DriveConstants.leftMotorPortID.length; motor++) {
      leftDriveTalonFX[motor] = new WPI_TalonFX(DriveConstants.leftMotorPortID[motor]);
      leftDriveTalonFX[motor].configFactoryDefault(); // reset the controller to defaults
      if (motor == 0) { // setup master
        leftDriveTalonFX[motor].set(ControlMode.PercentOutput, 0); // set the motor to Percent Output with Default of 0
        leftDriveTalonFX[motor].setInverted(false); // left side will NOT be inverted
      } else { // setup followers
        leftDriveTalonFX[motor].follow(leftDriveTalonFX[0]);
        leftDriveTalonFX[motor].setInverted(InvertType.FollowMaster); // set green lights when going forward
      }
    }

    // Engage brake mode
    driveTrainBrakeMode();

    drive = new DifferentialDrive(leftDriveTalonFX[0], rightDriveTalonFX[0]);

    // Prevent WPI drivetrain class from inverting input for right side motors
    // because we already inverted them
    // The new 2022 version of the libraries will stop inverting the motors by
    // default inyway
    drive.setRightSideInverted(false);

  }

  /**
   * Engage Brake Mode
   */
  public void driveTrainBrakeMode() {
    for (int motor = 0; motor < DriveConstants.rightMotorPortID.length; motor++) {
      rightDriveTalonFX[motor].setNeutralMode(NeutralMode.Brake);
    }
    for (int motor = 0; motor < DriveConstants.leftMotorPortID.length; motor++) {
      leftDriveTalonFX[motor].setNeutralMode(NeutralMode.Brake);
    }
  }

  public void driveTrainCoastMode() {
    for (int motor = 0; motor < DriveConstants.rightMotorPortID.length; motor++) {
      rightDriveTalonFX[motor].setNeutralMode(NeutralMode.Coast);
    }
    for (int motor = 0; motor < DriveConstants.leftMotorPortID.length; motor++) {
      leftDriveTalonFX[motor].setNeutralMode(NeutralMode.Coast);
    }
  }

  public void zeroDriveEncoders() {
    rightDriveTalonFX[0].setSelectedSensorPosition(0);
    leftDriveTalonFX[0].setSelectedSensorPosition(0);
    driveTrainCoastMode(); // TODO: figure out why this was introduced in 2020
  }

  /**
   * Drives the robot using arcade controls.
   *
   * @param fwd the commanded forward movement
   * @param rot the commanded rotation
   */
  public void arcadeDrive(double fwd, double rot) {
    drive.arcadeDrive(fwd, rot);
  }

  /**
   * We configure drivetrain for SimpleMagic here. Note that the drivetrain will
   * work without it. But using it may allow you to smooth the driving pattern and
   * make the robot more responsive
   */
  public void configureSimpleMagic() {

    // We assume we have the same number of left motors as we have the right ones
    for (int motor = 0; motor < DriveConstants.rightMotorPortID.length; motor++) {

      /* Set status frame periods to ensure we don't have stale data */
      rightDriveTalonFX[motor].setStatusFramePeriod(StatusFrame.Status_13_Base_PIDF0, 20,
          DriveConstants.configureTimeoutMs);
      leftDriveTalonFX[motor].setStatusFramePeriod(StatusFrame.Status_13_Base_PIDF0, 20,
          DriveConstants.configureTimeoutMs);
      rightDriveTalonFX[motor].setStatusFramePeriod(StatusFrame.Status_10_Targets, 20,
          DriveConstants.configureTimeoutMs);
      leftDriveTalonFX[motor].setStatusFramePeriod(StatusFrame.Status_10_Targets, 20,
          DriveConstants.configureTimeoutMs);
    }

    // Leading motors only

    /* Configure motor neutral deadband */
    rightDriveTalonFX[0].configNeutralDeadband(DriveConstants.NeutralDeadband, DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configNeutralDeadband(DriveConstants.NeutralDeadband, DriveConstants.configureTimeoutMs);

    /**
     * Max out the peak output (for all modes). However you can limit the output of
     * a given PID object with configClosedLoopPeakOutput().
     */
    leftDriveTalonFX[0].configPeakOutputForward(+1.0, DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configPeakOutputReverse(-1.0, DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configNominalOutputForward(0, DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configNominalOutputReverse(0, DriveConstants.configureTimeoutMs);

    rightDriveTalonFX[0].configPeakOutputForward(+1.0, DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configPeakOutputReverse(-1.0, DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configNominalOutputForward(0, DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configNominalOutputReverse(0, DriveConstants.configureTimeoutMs);

    /* FPID Gains for each side of drivetrain */
    leftDriveTalonFX[0].config_kP(DriveConstants.SLOT_0, DriveConstants.motionMagicPidP_Value,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].config_kI(DriveConstants.SLOT_0, DriveConstants.motionMagicPidI_Value,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].config_kD(DriveConstants.SLOT_0, DriveConstants.motionMagicPidD_Value,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].config_kF(DriveConstants.SLOT_0, DriveConstants.motionMagicPidF_Value,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].config_IntegralZone(DriveConstants.SLOT_0, DriveConstants.Izone_0,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configClosedLoopPeakOutput(DriveConstants.SLOT_0, DriveConstants.PeakOutput_0,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configAllowableClosedloopError(DriveConstants.SLOT_0, 5, DriveConstants.configureTimeoutMs);

    rightDriveTalonFX[0].config_kP(DriveConstants.SLOT_0, DriveConstants.motionMagicPidP_Value,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].config_kI(DriveConstants.SLOT_0, DriveConstants.motionMagicPidI_Value,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].config_kD(DriveConstants.SLOT_0, DriveConstants.motionMagicPidD_Value,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].config_kF(DriveConstants.SLOT_0, DriveConstants.motionMagicPidF_Value,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].config_IntegralZone(DriveConstants.SLOT_0, DriveConstants.Izone_0,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configClosedLoopPeakOutput(DriveConstants.SLOT_0, DriveConstants.PeakOutput_0,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configAllowableClosedloopError(DriveConstants.SLOT_0, 5, DriveConstants.configureTimeoutMs);

    /**
     * 1ms per loop. PID loop can be slowed down if need be. For example, - if
     * sensor updates are too slow - sensor deltas are very small per update, so
     * derivative error never gets large enough to be useful. - sensor movement is
     * very slow causing the derivative error to be near zero.
     */
    rightDriveTalonFX[0].configClosedLoopPeriod(0, DriveConstants.closedLoopPeriodMs,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configClosedLoopPeriod(0, DriveConstants.closedLoopPeriodMs, DriveConstants.configureTimeoutMs);

    /* Motion Magic Configurations */

    /**
     * Need to replace numbers with real measured values for acceleration and cruise
     * vel.
     */
    leftDriveTalonFX[0].configMotionAcceleration(DriveConstants.motionMagicAcceleration,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configMotionCruiseVelocity(DriveConstants.motionMagicCruiseVelocity,
        DriveConstants.configureTimeoutMs);
    leftDriveTalonFX[0].configMotionSCurveStrength(DriveConstants.motionMagicSmoothing);

    rightDriveTalonFX[0].configMotionAcceleration(DriveConstants.motionMagicAcceleration,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configMotionCruiseVelocity(DriveConstants.motionMagicCruiseVelocity,
        DriveConstants.configureTimeoutMs);
    rightDriveTalonFX[0].configMotionSCurveStrength(DriveConstants.motionMagicSmoothing);

  } // End configureDriveTrainControllersForSimpleMagic

  public void simpleMotionMagic(int leftEncoderVal, int rightEncoderVal) {
    // Test method that moves robot forward a given number of wheel rotations
    // Requires destination values for the encoders
    leftDriveTalonFX[0].set(ControlMode.MotionMagic, leftEncoderVal);
    rightDriveTalonFX[0].set(ControlMode.MotionMagic, rightEncoderVal);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
