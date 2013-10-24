package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.*;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import java.util.List;

public enum VosManagerMethod implements ManagerMethod {

	/*#
	 * Returns list of all VOs.
	 * 
	 * @return List<VirtualOrganization> Found VOs
	 */
  getVos {

    @Override
    public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getVosManager().getVos(ac.getSession());
    }
  },
  
  getAllVos {

    @Override
    public List<Vo> call(ApiCaller ac, Deserializer parms) throws PerunException {
      return ac.getVosManager().getAllVos(ac.getSession());
    }
  },
  
  /*#
	 * Deletes a VO.
	 * 
	 * @param vo int VO ID
	 */
/*#
	 * Deletes a VO (force).
	 * 
	 * @param vo int VO ID
	 * @param force int Force must be 1
	 */
  deleteVo {

    @Override
    public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();

      if(parms.contains("force")) { 
        ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")), true);
      } else {
        ac.getVosManager().deleteVo(ac.getSession(), ac.getVoById(parms.readInt("vo")));
      }
      return null;
    }
  },
  
  /*#
   * Creates a VO.
   * 
   * @param vo VirtualOrganization JSON VO class
   * @return VirtualOrganization Newly created VO
   */
  createVo {

    @Override
    public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();

      return ac.getVosManager().createVo(ac.getSession(), parms.read("vo", Vo.class));
    }
  },
  
  /*#
   * Updates a VO.
   * 
   * @param vo VirtualOrganization JSON VO class
   * @return VirtualOrganization Updated VO
   */
  updateVo {

    @Override
    public Vo call(ApiCaller ac, Deserializer parms) throws PerunException {
      ac.stateChangingCheck();

      return ac.getVosManager().updateVo(ac.getSession(), parms.read("vo", Vo.class));
    }
  },
  
  /*#
   * Returns a VO by a short name.
   * 
   * @param shortName String VO shortName
   * @return VirtualOrganization Found VO
   */
  getVoByShortName {

    @Override
    public Vo call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getVoByShortName(ac.getSession(), parms.readString("shortName"));
    }
  },
  
  /*#
   * Returns a VO by ID.
   * 
   * @param id int VO ID
   * @return VirtualOrganization Found VO
   */
  getVoById {

    @Override
    public Vo call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getVoById(ac.getSession(), parms.readInt("id"));
    }
  },
  
  /*#
   * Finds candidates for a VO.
   * 
   * @param vo int VO ID
   * @param searchString String String to search by
   * @return List<Candidate> List of candidates
   */
  /*#
   * Finds candidates for a VO. Maximum results specified.
   * 
   * @param vo int VO ID
   * @param searchString String String to search by
   * @param maxNumOfResults int Maximum results
   * @return List<Candidate> List of candidates
   */
  findCandidates {

    @Override
    public List<Candidate> call(ApiCaller ac, Deserializer parms) throws PerunException {
      if (parms.contains("maxNumOfResults")) {
        return ac.getVosManager().findCandidates(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            parms.readString("searchString"),
            parms.readInt("maxNumOfResults"));
      } else {
        return ac.getVosManager().findCandidates(ac.getSession(),
            ac.getVoById(parms.readInt("vo")),
            parms.readString("searchString"));
      }
    }
  },
  
  /*#
   * Adds an admin to a VO.
   * 
   * @param vo int VO ID
   * @param user int User ID
  /*#
  *  Adds a group admin to a VO.
  * 
  *  @param vo int VO ID
  *  @param authorizedGroup int Group ID
  */
    addAdmin {
        @Override
        public Void call(ApiCaller ac, Deserializer parms)
                throws PerunException {
            ac.stateChangingCheck();
            if (parms.contains("user")) {
                ac.getVosManager().addAdmin(ac.getSession(),
                        ac.getVoById(parms.readInt("vo")),
                        ac.getUserById(parms.readInt("user")));
            } else {
                ac.getVosManager().addAdmin(ac.getSession(),
                        ac.getVoById(parms.readInt("vo")),
                        ac.getGroupById(parms.readInt("authorizedGroup")));
            }
            return null;
        }
    },
  
  /*#
   * Removes an admin from a VO.
   * 
   * @param vo int VO ID
   * @param user int User ID
  /*#
  *  Removes a group admin from VO.
  * 
  *  @param vo int VO ID
  *  @param group int Group ID
  */
    removeAdmin {
        @Override
        public Void call(ApiCaller ac, Deserializer parms)
                throws PerunException {
            ac.stateChangingCheck();
            if (parms.contains("user")) {
                ac.getVosManager().removeAdmin(ac.getSession(),
                        ac.getVoById(parms.readInt("vo")),
                        ac.getUserById(parms.readInt("user")));
            } else {
                ac.getVosManager().removeAdmin(ac.getSession(),
                        ac.getVoById(parms.readInt("vo")),
                        ac.getGroupById(parms.readInt("authorizedGroup")));
            }
            return null;
        }
    },
  
  /*#
 	 * Returns administrators of a VO.
 	 * 
 	 * @param vo int VO ID
 	 * @return List<User> VO admins
 	 */
  getAdmins {

    @Override
    public List<User> call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getAdmins(ac.getSession(),
          ac.getVoById(parms.readInt("vo")));
    }
  },
  
   /*#
 	 * Returns direct administrators of a VO.
 	 * 
 	 * @param vo int VO ID
 	 * @return List<User> VO admins
 	 */
  getDirectAdmins {

    @Override
    public List<User> call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getDirectAdmins(ac.getSession(),
          ac.getVoById(parms.readInt("vo")));
    }
  },
  
   /*#
 	 * Returns group administrators of a VO.
 	 * 
 	 * @param vo int VO ID
 	 * @return List<User> VO admins
 	 */
  getAdminGroups {

    @Override
    public List<Group> call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getAdminGroups(ac.getSession(),
          ac.getVoById(parms.readInt("vo")));
    }
  },
  
  /*#
	 * Returns administrators of a VO.
	 * 
	 * @param vo int VO ID
	 * @return List<RichUser> VO admins
	 */

  getRichAdmins {

    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getRichAdmins(ac.getSession(),
          ac.getVoById(parms.readInt("vo")));
    }
  },
  /*#
 	 * Returns administrators of a VO with additional information.
 	 * 
 	 * @param vo int VO ID
 	 * @return List<RichUser> VO admins
 	 */
  getRichAdminsWithAttributes {

    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms)
    throws PerunException {
      return ac.getVosManager().getRichAdminsWithAttributes(ac.getSession(),
          ac.getVoById(parms.readInt("vo")));
    }
  },
  
  getRichAdminsWithSpecificAttributes {
    
    @Override
    public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {

      return ac.getVosManager().getRichAdminsWithSpecificAttributes(ac.getSession(),
          ac.getVoById(parms.readInt("vo")),
          parms.readList("specificAttributes", String.class));
    }
  };
}
