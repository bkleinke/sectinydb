// $Id: SecureSumM.nc,v 1.5 2003/10/07 21:46:22 idgay Exp $

/*									tab:4
 * "Copyright (c) 2000-2003 The Regents of the University  of California.  
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice, the following
 * two paragraphs and the author appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
 *
 * Copyright (c) 2002-2003 Intel Corporation
 * All rights reserved.
 *
 * This file is distributed under the terms in the attached INTEL-LICENSE     
 * file. If you do not find these files, copies can be found by writing to
 * Intel Research Berkeley, 2150 Shattuck Avenue, Suite 1300, Berkeley, CA, 
 * 94704.  Attention:  Intel License Inquiry.
 */
/**
 * Implements SecureSum aggregate (modified from AVG)
 *
 * Author:	Eugene Shvets
 * @author Eugene Shvets
 */
includes Aggregates;

module SecureSumM {
	provides {
		interface Aggregate;
	}
}

implementation {

	command result_t Aggregate.merge(char *destdata, char *mergedata, ParamList *params, ParamVals *paramValues) {
		SecureSumData *dest  = (SecureSumData *)destdata;
		SecureSumData *merge = (SecureSumData *)mergedata;

		dest->sum += merge->sum;
		dest->csum += merge->csum;
		dest->count += merge->count;
		
		if (TOS_LOCAL_ADDRESS==0)
			dbg(DBG_USR1, "HEHEHE sum %d csum %d count %d\n\n",dest->sum, dest->csum, dest->count);

		return SUCCESS;
	}
	
	command result_t Aggregate.update(char *destdata, char* value, ParamList *params, ParamVals *paramValues) {
		SecureSumData *dest  = (SecureSumData *)destdata;
		int16_t val = *(int16_t *)value;
		int16_t cval = *(int16_t *) (value+2);
		
		dest->sum += val;
		dest->csum += val;
		dest->count++;
		
		return SUCCESS;
	}

	command result_t Aggregate.init(char *data, ParamList *params, ParamVals *paramValues, bool isFirstTime){
		SecureSumData *mydata = (SecureSumData *)data;

		bzero(data,AGG_DATA_LEN);
		mydata->sum = 0;
		mydata->csum = 0;
		mydata->count = 0;
		
		return SUCCESS;
	}

	command uint16_t Aggregate.stateSize(ParamList *params, ParamVals *paramValues) {
		return sizeof(SecureSumData);
	}

	command bool Aggregate.hasData(char *data, ParamList *params, ParamVals *paramValues) {
		return TRUE;
	}

	command TinyDBError Aggregate.finalize(char *data, char *result_buf, ParamList *params, ParamVals *paramValues) {
		SecureSumData *mydata = (SecureSumData *)data;
		
		*(int16_t *)result_buf = mydata->sum;
		*(int16_t *)(result_buf+2) = mydata->csum;
		*(int16_t *)(result_buf+4) = mydata->count;
		
		return err_NoError;
	}
	
	command AggregateProperties Aggregate.getProperties() {
		return 0;
	}
	
}

