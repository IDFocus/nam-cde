package nl.idfocus.test.nam.nxpe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novell.nxpe.NxpeException;
import com.novell.nxpe.NxpeGroupedParameter;
import com.novell.nxpe.NxpeParameter;
import com.novell.nxpe.NxpeParameterList;

public class TestNxpeParameterList implements NxpeParameterList
{
	private List<NxpeParameter> params;

	public TestNxpeParameterList()
	{
		params = new ArrayList<NxpeParameter>();
	}

	public TestNxpeParameterList addParameter( NxpeParameter param )
	{
		params.add(param);
		return this;
	}

	public NxpeGroupedParameter getGroupedParameter(int arg0, int arg1, int arg2)
			throws NxpeException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public NxpeParameter getParameter(int iname) throws NxpeException
	{
		for( NxpeParameter param : params )
			if( param.getEnumerativeValue() == iname)
				return param;
		throw new NxpeException("No Parameter has number "+iname);
	}

	public Iterator<NxpeParameter> iterator() throws NxpeException
	{
		return params.iterator();
	}

	public Iterator<NxpeGroupedParameter> iterator(int arg0) throws NxpeException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<NxpeGroupedParameter> iterator(int arg0, int arg1) throws NxpeException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
