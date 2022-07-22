import React from 'react'
import './contact.css'
import {MdOutlineEmail} from 'react-icons/md'
import {FaTelegramPlane} from 'react-icons/fa'
import { useRef } from 'react';
import emailjs from 'emailjs-com'

const Contact = () => {

  const form = useRef();

  const sendEmail = (e) => {
    e.preventDefault();

    emailjs.sendForm('service_cesptbs', 'template_rky83sc', form.current, '7LV4CAHPoFe7droq0')
      .then((result) => {
          console.log(result.text);
      }, (error) => {
          console.log(error.text);
      });

    e.target.reset()
  };

  return (
    <section id='contact'>
      <h5>Остались вопросы?</h5>  
      <h2>Напишите нам!</h2>

      <div className="container contact__container">
        <div className="contact__options">
        <article className="contact__option">
          <MdOutlineEmail className='contact__option-icon'/>
          <h4>Email</h4>
          <h5>example@mail.ru</h5>
          <a href="mailto:vladimir-markovich@mail.ru" target="_blank">Написать</a>
        </article> 
        <article className="contact__option">
          <FaTelegramPlane className='contact__option-icon'/>
          <h4>Telegram</h4>
          <h5>@ScyTech</h5>
          <a href="https://t.me/vm5_ball" target="_blank">Написать</a>
        </article>  
        </div>  
        <form ref={form} onSubmit={sendEmail}>
          <input type="text" name='name' placeholder='Ваше полное имя' required />
          <input type="email" name='email' placeholder='Ваш email' required />
          <textarea name="message" rows="5" placeholder='Ваше сообщение' required></textarea>
          <button type='submit' className='btn btn-primary'>Отправить сообщение</button>
        </form>
      </div>
    </section>
  )
}

export default Contact