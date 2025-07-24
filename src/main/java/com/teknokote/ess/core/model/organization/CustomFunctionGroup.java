package com.teknokote.ess.core.model.organization;

import com.teknokote.core.model.ESSEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CustomFunctionGroup extends ESSEntity<Long,User>
{
   private static final long serialVersionUID = 8_224_234_998_640_362_111L;
   @Column(nullable = false)
   private String code;
   private String description;
   @Enumerated(EnumType.STRING)
   private EnumFunctionalScope scope;
   @ManyToOne
   private User relatedUser;
   @Column(name="related_user_id",insertable = false, updatable = false)
   private Long relatedUserId;
   @ManyToMany
   @JoinTable(name = "custom_function_group_functions",joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "function_id"))
   private Set<Function> functions=new HashSet<>();

   @Override
   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(o == null || getClass() != o.getClass()) return false;

      CustomFunctionGroup that = (CustomFunctionGroup) o;

      return code.equals(that.code);
   }

   @Override
   public int hashCode()
   {
      return code.hashCode();
   }
}
