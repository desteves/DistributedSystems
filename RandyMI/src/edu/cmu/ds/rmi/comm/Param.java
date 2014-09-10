package edu.cmu.ds.rmi.comm;

import java.io.Serializable;


/**
 * A method's parameter or return type
 * @author Linne
 *
 */
public class Param  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8631318374605372730L;
	private Class type;
	private Object value;

	public Param(Class type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	public Param() {
		super();
	}

	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Param [type=" + type + ", value=" + value + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Param other = (Param) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
