# 스프링 Validation

----

### 검증

~~~java
@PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        log.info("bindingResult = {}", bindingResult);

        //특정 필드가 아닌 복합 룰 검증
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < ITEM_MIN_TOTAL_PRICE) {
                bindingResult.reject("totalPriceMin", new Object[]{ITEM_MIN_TOTAL_PRICE, resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            return "validation/v4/addForm";
        }

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

        //성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }
~~~
ModelAttribute 파라미터 옆에 BindingResult 를 통해 (옆에 두어야 BindingResultrk Model에 Bingding 된다.) 검증 기능을 수행할 수 있다.
FieldError와 ObjectError로 나뉘어 처리한다.

~~~html
<div th:if="${#fields.hasGlobalErrors()}">
  <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">글로벌 오류 메시지</p> 
</div>
~~~
ObjectError 는 Thymeleaf 에서 #fields.hasGlobalErrors() 를 통해 확인 가능.

~~~html
<div>
  <label for="itemName" th:text="#{label.item.itemName}">상품명</label>
  <input type="text" id="itemName" th:field="*{itemName}" th:errorclass="field-error" class="form-control" placeholder="이름을 입력하세요">
  <div class="field-error" th:errors="*{itemName}">상품명 오류</div>
</div>
~~~
Field 에러는 th:errors="${object.fieldName}" 을 통해 해당 검증이 실패했을 시 받아올 수 있다. (에러가 있을 때만 해당 태크 출력)
(th:errorclass 를 통해 에러가 있을 시 클래스를 추가할 수 있다.)


스프링에서 사용하는 DefaultMessageCodesResolver 에 의해 아래와 같은 메시지들이 자동 생성된다.
#### ObjectError
~~~
1.: code + "." + object name 
2.: code

예) 오류 코드: required, object name: item 
1.: required.item
2.: required
~~~

#### FieldError
~~~
1.: code + "." + object name + "." + field
2.: code + "." + field
3.: code + "." + field type
4.: code

예) 오류 코드: typeMismatch, object name "user", field "age", field type: int 
1. "typeMismatch.user.age"
2. "typeMismatch.age"
3. "typeMismatch.int"
4. "typeMismatch"
~~~

## Bean Validation

~~~java
public class Item {
      private Long id;
      
      @NotBlank
      private String itemName;
      
      @NotNull
      @Range(min = 1000, max = 1000000)
      private Integer price;
      
      @NotNull
      @Max(9999)
      private Integer quantity;
      //...
}
~~~
Bean Validation 기능을 이용하여 위와 같은 Annotation을 사용하여 편라하게 검증 수행 가능

##### 다음과 같은 의존성 추가 필요
~~~gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
~~~
스프링 부트가 spring-boot-starter-validation 라이브러리를 넣으면 자동으로 Bean Validator를 인지하고 스프링에 통합한다.

## NotNull, NotEmpty, NotBlank 의 차이점
1. NotNull : 단순히 Null만 아니라면 검증에 통과
2. 비어있는 값. ""이 아니라면 검증 통과 (" ", "    " 등등은 통과 가능)
3. 값이 있어야지만 검증 통과 (" ", "      " 등등 모두 통과 불가능)
-> NotBlank 가 제일 심화된 검증이라고 보면됨

~~~java
@PostMapping("/add")
  public String addItem(@Validated @ModelAttribute Item item, BindingResult
  bindingResult, RedirectAttributes redirectAttributes) 
~~~
검증하려는 객체에 '@Validated' 또는 '@Valid' 를 붙여주기만 하면 된다.


## 등록과 수정의 검증 요구사항이 다를 경우..?
Bean Validation만으로 다른 검증 요구사항을 적용하기 위해서 groups 라는 기능이 있지만 실질적으로 잘 사용하지 않음.
대신에 등록용 DTO, 수정용 DTO 를 따로 분리하여 각각에 맞는 검증 등록

## @MoelAttribute vs @RequestBody
HTTP 요청 파리미터를 처리하는 @ModelAttribute 는 각각의 필드 단위로 세밀하게 적용된다. 그래서 특정 필드에 타입이 맞지 않는 오류가 발생해도 나머지 필드는 정상 처리할 수 있었다. 
HttpMessageConverter 는 @ModelAttribute 와 다르게 각각의 필드 단위로 적용되는 것이 아니라, 전체 객체 단위로 적용된다.
따라서 메시지 컨버터의 작동이 성공해서 Item 객체를 만들어야 @Valid , @Validated 가 적용된다.
@ModelAttribute 는 필드 단위로 정교하게 바인딩이 적용된다. 특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩 되고, Validator를 사용한 검증도 적용할 수 있다.
@RequestBody 는 HttpMessageConverter 단계에서 JSON 데이터를 객체로 변경하지 못하면 이후 단계 자체가 진행되지 않고 예외가 발생한다. 컨트롤러도 호출되지 않고, Validator도 적용할 수 없다.


----
출처: 김영한 님의 인프런 강의 - [스프링 MVC 2편 - 백엔드 웹 개발 활용 기술](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-mvc-2)
