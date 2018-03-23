package com.jld.base.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jld.base.core.security.UserPreferences;
import com.jld.base.core.utils.Constants;
import com.jld.base.dao.PictureDao;
import com.jld.base.dao.UserDao;
import com.jld.base.dao.UsergroupDao;
import com.jld.base.dao.UserpreferenceDao;
import com.jld.base.form.UserCreateForm;
import com.jld.base.form.UserSearchForm;
import com.jld.base.model.Picture;
import com.jld.base.model.User;
import com.jld.base.model.Usergroup;
import com.jld.base.model.Userpreference;
import com.jld.base.model.vo.ModelAttribute;
import com.jld.base.model.vo.ModelFilter;
import com.jld.base.pagination.PaginationConfiguration;
import com.jld.base.pagination.PaginationResultInfo;
import com.jld.base.pojo.UserDescription;

@Service
@Transactional
public class UserService {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UsergroupDao usergroupDao;
	
	@Autowired
	private PictureDao pictureDao;
	
	@Autowired
	private UserpreferenceDao userpreferenceDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	public List<User> getUserList() {
		List<User> userList = userDao.findAll();
		return userList;
	}
	
	public PaginationResultInfo<User> searchUsersPaginated(UserSearchForm form) {
		PaginationResultInfo<User> resultInfo = new PaginationResultInfo<User>();

		ModelFilter modelFilter = new ModelFilter();
		if(StringUtils.isNotEmpty(form.getUsername())) {
			modelFilter.getAttributes().add(new ModelAttribute("username", form.getUsername() + "%", Constants.MODEL_OPERATION_LIKE));
		}
		if(StringUtils.isNotEmpty(form.getEmail())) {
			modelFilter.getAttributes().add(new ModelAttribute("email", form.getEmail() + "%", Constants.MODEL_OPERATION_LIKE));
		}
		if(form.getGroup() != null) {
			modelFilter.getAttributes().add(new ModelAttribute("usergroup.idusergroup", String.valueOf(form.getGroup().intValue()), Constants.MODEL_OPERATION_EQUAL));
		}
		modelFilter.setOrderBy("username", Constants.MODEL_ORDER_ASC);
		modelFilter.setFirstResult(form.getFrom());
		modelFilter.setMaxResults(form.getPageSize());
		
		resultInfo.setListaResultados(userDao.findList(modelFilter));//userDao.findByForm(form));
		resultInfo.setTotalResultados(userDao.findCount(modelFilter));//userDao.countByForm(form));
		
		return resultInfo;
	}

	public List<Usergroup> getAllUsergroups() {
		return usergroupDao.findAll();
	}
	
	@Transactional
	public void addNewUser(String baseRepositoryPath, UserCreateForm form) throws Exception {
		User newUser = new User();
		
		newUser.setUsername(form.getUsername());
		newUser.setEmail(form.getEmail());
		newUser.setName(form.getName());
		newUser.setSurname1(form.getSurname1());
		newUser.setSurname2(form.getSurname2());
		newUser.setPassword(passwordEncoder.encode(form.getPassword1()));
		
		Usergroup userGroup = usergroupDao.findOne(form.getGroup());
		newUser.setUsergroup(userGroup);
		
		// Check that the user does not exist:
		ModelFilter modelFilter = new ModelFilter();
		modelFilter.getAttributes().add(new ModelAttribute("username", form.getUsername(), Constants.MODEL_OPERATION_EQUAL));
		if(userDao.findCount(modelFilter) > 0) {
			// TODO: User already exists
			
		}
		else {
			
			String repositoryPath = File.separator + form.getUsername();			
			
			// Default user preferences:
			Userpreference userPreferences = new Userpreference();
			userPreferences.setRepositorypath(repositoryPath);
			ModelFilter filter = new ModelFilter();
			filter.getAttributes().add(new ModelAttribute("filekey", "anonymous-user", Constants.MODEL_OPERATION_EQUAL));
			filter.getAttributes().add(new ModelAttribute("fileextension", "png", Constants.MODEL_OPERATION_EQUAL));
			List<Picture> profilePicture = pictureDao.findPicture(filter);
			if(profilePicture != null && profilePicture.size() > 0) {
				userPreferences.setPicture(profilePicture.get(0));
			}
			userPreferences.setNumresultsinpage(PaginationConfiguration.DEFAULT_PAGE_SIZE);
			if(newUser.getUserpreferences() == null) {
				newUser.setUserpreferences(new ArrayList<Userpreference>());
			}
			userPreferences.setUser(newUser);	// Preferences are created in CASCADE (see one-to-many relation in User model class)
			newUser.getUserpreferences().add(userPreferences);
			
			// Save user:
			userDao.create(newUser);
			
			// Create user personal repository path:
			File repo = new File(baseRepositoryPath + File.separator + repositoryPath);
			if(!repo.exists()) {
				repo.mkdir();
			}
		}
	}

	public UserCreateForm getUserInfoById(Integer idUser) {
		UserCreateForm form = new UserCreateForm();
		
		User user = userDao.findOne(idUser);
		if(user != null) {
			form.setEmail(user.getEmail());
			form.setName(user.getName());
			form.setSurname1(user.getSurname1());
			form.setSurname2(user.getSurname2());
			form.setGroup(user.getUsergroup().getIdusergroup());
			form.setUserid(user.getIduser());
			form.setUsername(user.getUsername());
		}
		
		return form;
	}
	
	@Transactional
	public void updateUser(UserCreateForm form) {
		User user = userDao.findOne(form.getUserid());
		
		user.setUsername(form.getUsername());
		user.setName(form.getName());
		user.setSurname1(form.getSurname1());
		user.setSurname2(form.getSurname2());
		user.setEmail(form.getEmail());
		user.setUsergroup(usergroupDao.findOne(form.getGroup()));
		
		userDao.updateOne(user);
	}
	
	@Transactional
	public void deleteUserById(Integer idUser) throws Exception {
		try {
			userDao.deleteById(idUser);
		} catch(Exception e) {
			throw e;
		}
	}

	public UserDescription getUserInfoByUsername(String userName) {

		UserDescription userDescription = new UserDescription();
		
		User user = userDao.findUserByField("username", userName);
		
		if(user != null) {			
			userDescription.setEmail(user.getEmail());
			userDescription.setName(user.getName());
			userDescription.setSurname1(user.getSurname1());
			userDescription.setSurname2(user.getSurname2());
			userDescription.setGroup(user.getUsergroup().getIdusergroup());
			userDescription.setGroupStr(user.getUsergroup().getGroupname());
			userDescription.setUserid(user.getIduser());
			userDescription.setUsername(user.getUsername());

			ModelFilter modelFilter = new ModelFilter();
			modelFilter.getAttributes().add(new ModelAttribute("user.username", userName, Constants.MODEL_OPERATION_EQUAL));
			Userpreference userPreferences = userpreferenceDao.findUserpreference(userName);
			if(userPreferences.getPicture() != null && StringUtils.isNotEmpty(userPreferences.getPicture().getFilekey())) {
				userDescription.setProfilePicture(userPreferences.getPicture().getFilekey() + "." + userPreferences.getPicture().getFileextension());
			}
			/*List<Picture> listProfilePicture = pictureDao.findPicture(modelFilter);
			if(listProfilePicture != null && listProfilePicture.size() > 0) {
				userDescription.setProfilePicture(listProfilePicture.get(0).getFilekey() + "." + listProfilePicture.get(0).getFileextension());
			}
			else {
				userDescription.setProfilePicture("bp" + File.separator + "anonymous-user.png");
			}*/
		}
		
		return userDescription;
	}

	public UserPreferences getUserPreferences(String userName) {
		UserPreferences userPreferences = new UserPreferences();
		
		Userpreference storedPreferences = userpreferenceDao.findUserpreference(userName);
		
		userPreferences.setRepositoryPath(storedPreferences.getRepositorypath());
		if(storedPreferences.getPicture() != null) {
			userPreferences.setProfilePicture(storedPreferences.getPicture().getFilekey() + "." + storedPreferences.getPicture().getFileextension());
		}
		else {
			
		}
		userPreferences.setNumResultsInPage(storedPreferences.getNumresultsinpage());
		
		return userPreferences;
	}
	
	public void refreshUserPreferences() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		com.jld.base.core.security.User loguedInUser = (com.jld.base.core.security.User) authentication.getPrincipal();
		loguedInUser.setUserPreferences(getUserPreferences(loguedInUser.getUsername()));
	}
}
