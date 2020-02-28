package ramal.facil.model;

import com.liferay.portal.kernel.model.User;
import java.util.List;

/**
 * Descrição do Fonte
 * 
 * @author 10 de abr de 2019: Carlos.Guedes <DD>
 */
public class Area {

   private String Nome;
   private List<User> users;

   /**
    * @TODO Comentar Método
    * @return
    */
   public String getNome() {
      return Nome;
   }

   /**
    * @TODO Comentar Método
    * @return
    */
   public List<User> getUsers() {
      return users;
   }

   /**
    * @TODO Comentar Método
    * @param nome
    */
   public void setNome(String nome) {
      Nome = nome;
   }

   public void addUser(User user) {
	   this.users.add(user);
   }
   /**
    * @TODO Comentar Método
    * @param users
    */
   public void setUsers(List<User> users) {
      this.users = users;
   }
}
