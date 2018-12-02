/**
 * demo1.js
 * http://www.codrops.com
 *
 * Licensed under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Copyright 2017, Codrops
 * http://www.codrops.com
 */
{
    const DOM = {};
    DOM.enter = document.querySelector('.enter');
    DOM.enter1 = document.querySelector('.enter1');
    DOM.enter2 = document.querySelector('.enter2');
    charming(DOM.enter);
    charming(DOM.enter1);
    charming(DOM.enter2);
    DOM.enterLetters = Array.from(DOM.enter.querySelectorAll('span'));
    DOM.enterLetters1 = Array.from(DOM.enter1.querySelectorAll('span'));
    DOM.enterLetters2 = Array.from(DOM.enter2.querySelectorAll('span'));

    const init = () => {
    DOM.enter.addEventListener('mouseenter', enterHoverInFn);
    DOM.enter.addEventListener('mouseleave', enterHoverOutFn);
    DOM.enter1.addEventListener('mouseenter', enterHoverInFn1);
    DOM.enter1.addEventListener('mouseleave', enterHoverOutFn1);
    DOM.enter2.addEventListener('mouseenter', enterHoverInFn2);
    DOM.enter2.addEventListener('mouseleave', enterHoverOutFn2);
    };


    let isActive;
    let enterTimeout;

    const enterHoverInFn = () => enterTimeout = setTimeout(() => {
        isActive = true;
        anime.remove(DOM.enterLetters);
        anime({
            targets: DOM.enterLetters,
            delay: (t,i) => i*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity:[
                {value: 0, duration: 150, easing: 'linear'},
                {value: 1, duration: 150, easing: 'linear'}
            ],
            color: {
                    value: '#ff963b',
                    duration: 1,
                    delay: (t,i,l) => i*15+150
            }
        });
    }, 50);

    const enterHoverInFn1 = () => enterTimeout = setTimeout(() => {
        isActive = true;
        anime.remove(DOM.enterLetters1);
        anime({
            targets: DOM.enterLetters1,
            delay: (t,i) => i*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity:[
                {value: 0, duration: 150, easing: 'linear'},
                {value: 1, duration: 150, easing: 'linear'}
            ],
            color: {
                    value: '#ff963b',
                    duration: 1,
                    delay: (t,i,l) => i*15+150
            }
        });
    }, 50);

    const enterHoverInFn2 = () => enterTimeout = setTimeout(() => {
        isActive = true;
        anime.remove(DOM.enterLetters2);
        anime({
            targets: DOM.enterLetters2,
            delay: (t,i) => i*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity:[
                {value: 0, duration: 150, easing: 'linear'},
                {value: 1, duration: 150, easing: 'linear'}
            ],
            color: {
                    value: '#ff963b',
                    duration: 1,
                    delay: (t,i,l) => i*15+150
            }
        });
    }, 50);

    const enterHoverOutFn = () => {
    clearTimeout(enterTimeout);
        if( !isActive ) return;
        isActive = false;

        anime.remove(DOM.enterLetters);
        anime({
            targets: DOM.enterLetters,
            delay: (t,i,l) => (l-i-1)*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity: [
                {value: 0, duration: 400, easing: 'linear'},
                {value: 1, duration: 400, easing: 'linear'}
            ],
            color: {
                value: '#ffffff',
                duration: 1,
                delay: (t,i,l) => (l-i-1)*15+150
            }
        });
    };

    const enterHoverOutFn1 = () => {
    clearTimeout(enterTimeout);
        if( !isActive ) return;
        isActive = false;

        anime.remove(DOM.enterLetters1);
        anime({
            targets: DOM.enterLetters1,
            delay: (t,i,l) => (l-i-1)*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity: [
                {value: 0, duration: 400, easing: 'linear'},
                {value: 1, duration: 400, easing: 'linear'}
            ],
            color: {
                value: '#ffffff',
                duration: 1,
                delay: (t,i,l) => (l-i-1)*15+150
            }
        });
    };

        const enterHoverOutFn2 = () => {
            clearTimeout(enterTimeout);
        if( !isActive ) return;
        isActive = false;

        anime.remove(DOM.enterLetters2);
        anime({
            targets: DOM.enterLetters2,
            delay: (t,i,l) => (l-i-1)*15,
            translateY: [
                {value: 10, duration: 150, easing: 'easeInQuad'},
                {value: [-10,0], duration: 150, easing: 'easeOutQuad'}
            ],
            opacity: [
                {value: 0, duration: 400, easing: 'linear'},
                {value: 1, duration: 400, easing: 'linear'}
            ],
            color: {
                value: '#ffffff',
                duration: 1,
                delay: (t,i,l) => (l-i-1)*15+150
            }
        });
    };

    init();
};