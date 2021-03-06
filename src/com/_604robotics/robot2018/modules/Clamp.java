    package com._604robotics.robot2018.modules;

import com._604robotics.robot2018.constants.Ports;
import com._604robotics.robotnik.Action;
import com._604robotics.robotnik.Module;
import com._604robotics.robotnik.Output;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Clamp extends Module {
	private final DoubleSolenoid solenoid = new DoubleSolenoid(Ports.CLAMP_A, Ports.CLAMP_B);
	
	private boolean clamping = false;
	
	public Output<Boolean> isClamped = addOutput("Clamping", this::isClamped);
	
	public boolean isClamped() {
		return clamping;
	}
	
	public Action retract = new Retract();
	public Action extend = new Extend();
	
	public class Retract extends Action {
		public Retract() {
			super(Clamp.this, Retract.class);
		}
		
		@Override
		public void run() {
			solenoid.set(Value.kReverse);
			clamping = true;
		}
	}
	
	public class Extend extends Action {
		public Extend() {
			super(Clamp.this, Extend.class);
		}
		
		@Override
		public void run() {
			solenoid.set(Value.kForward);
			clamping = false;
		}
	}
	
	public class HoldRetract extends Action {
		public HoldRetract() {
			super(Clamp.this, HoldRetract.class);
		}
		
		@Override
		public void begin() {
			setDefaultAction(retract);
		}
		
		@Override
		public void run() {
			solenoid.set(Value.kReverse);
			clamping = false;
		}
	}
	
	public class HoldExtend extends Action {
		public HoldExtend() {
			super(Clamp.this, HoldExtend.class);
		}
		
		@Override
		public void begin() {
			setDefaultAction(extend);
		}
		
		@Override
		public void run() {
			solenoid.set(Value.kForward);
			clamping = true;
		}	
	}
	
	public Clamp() {
		super(Clamp.class);
		this.setDefaultAction(retract);
	}
}
